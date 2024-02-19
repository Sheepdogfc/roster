package org.acme.callcenter.service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import ai.timefold.solver.core.api.solver.change.ProblemChange;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.solver.change.AddCallProblemChange;
import org.acme.callcenter.solver.change.PinCallProblemChange;
import org.acme.callcenter.solver.change.ProlongCallByMinuteProblemChange;
import org.acme.callcenter.solver.change.RemoveCallProblemChange;

@ApplicationScoped
public class SolverService {

    private final SolverManager<CallCenter, String> solverManager;
    public static final String SINGLETON_ID = "1";

    private final BlockingQueue<WaitingProblemChange> waitingProblemChanges = new LinkedBlockingQueue<>();

    @Inject
    public SolverService(SolverManager<CallCenter, String> solverManager) {
        this.solverManager = solverManager;
    }

    private void pinCallAssignedToAgents(List<Call> calls) {
        calls.stream()
                .filter(call -> !call.isPinned()
                        && call.getPreviousCallOrAgent() != null
                        && call.getPreviousCallOrAgent() instanceof Agent)
                .map(PinCallProblemChange::new)
                .forEach(problemChange -> solverManager.addProblemChange(SINGLETON_ID, problemChange));
    }

    public void startSolving(CallCenter inputProblem,
            Consumer<CallCenter> bestSolutionConsumer, Consumer<Throwable> errorHandler) {
        solverManager.solveBuilder()
                .withProblemId(SINGLETON_ID)
                .withProblemFinder(id -> inputProblem)
                .withBestSolutionConsumer(bestSolution -> {
                    if (bestSolution.isFeasible()) {
                        bestSolutionConsumer.accept(bestSolution);
                        pinCallAssignedToAgents(bestSolution.getCalls());
                    }
                })
                .withExceptionHandler((id, error) -> errorHandler.accept(error)).run();

        for (WaitingProblemChange waitingProblemChange : waitingProblemChanges) {
            CompletableFuture<Void> changeInProgress =
                    solverManager.addProblemChange(SINGLETON_ID, waitingProblemChange.getProblemChange());
            changeInProgress.thenRun(() -> waitingProblemChange.getCompletion().complete(null));
        }
        waitingProblemChanges.clear();
    }

    public void stopSolving() {
        solverManager.terminateEarly(SINGLETON_ID);
    }

    public boolean isSolving() {
        return solverManager.getSolverStatus(SINGLETON_ID) != SolverStatus.NOT_SOLVING;
    }

    public CompletableFuture<Void> addCall(Call call) {
        return registerProblemChange(new AddCallProblemChange(call));
    }

    public CompletableFuture<Void> removeCall(String callId) {
        return registerProblemChange(new RemoveCallProblemChange(callId));
    }

    public CompletableFuture<Void> prolongCall(String callId) {
        return registerProblemChange(new ProlongCallByMinuteProblemChange(callId));
    }

    private CompletableFuture<Void> registerProblemChange(ProblemChange<CallCenter> problemChange) {
        if (isSolving()) {
            return solverManager.addProblemChange(SINGLETON_ID, problemChange);
        } else {
            /*
             * Expose a temporary CompletableFuture that will get completed once the solver is started again
             * and processes the change.
             */
            CompletableFuture<Void> completion = new CompletableFuture<>();
            waitingProblemChanges.add(new WaitingProblemChange(completion, problemChange));
            return completion;
        }
    }

    private static class WaitingProblemChange {
        private final CompletableFuture<Void> completion;
        private final ProblemChange<CallCenter> problemChange;

        public WaitingProblemChange(CompletableFuture<Void> completion, ProblemChange<CallCenter> problemChange) {
            this.completion = completion;
            this.problemChange = problemChange;
        }

        public CompletableFuture<Void> getCompletion() {
            return completion;
        }

        public ProblemChange<CallCenter> getProblemChange() {
            return problemChange;
        }
    }
}
