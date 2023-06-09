package org.acme.vehiclerouting.rest;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Path("route-plans")
public class VehicleRoutingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleRoutingResource.class);

    private final SolverManager<VehicleRoutingSolution, String> solverManager;

    // TODO: Without any "time to live", the map may eventually grow out of memory.
    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    // Workaround to make Quarkus CDI happy. Do not use.
    public VehicleRoutingResource() {
        this.solverManager = null;
    }

    @Inject
    public VehicleRoutingResource(SolverManager<VehicleRoutingSolution, String> solverManager) {
        this.solverManager = solverManager;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.TEXT_PLAIN)
    public String solve(VehicleRoutingSolution problem) {
        String jobId = UUID.randomUUID().toString();
        jobIdToJob.put(jobId, Job.newRoutePlan(problem));
        solverManager.solveAndListen(jobId,
                jobId_ -> jobIdToJob.get(jobId).routePlan,
                solution -> jobIdToJob.put(jobId, Job.newRoutePlan(solution)),
                (jobId_, exception) -> jobIdToJob.put(jobId, Job.error(jobId_, exception)));
        return jobId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}")
    public VehicleRoutingSolution getRoutePlan(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId,
            @QueryParam("retrieve") Retrieve retrieve) {
        retrieve = retrieve == null ? Retrieve.FULL : retrieve;
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new VehicleRoutingSolverException(jobId, Response.Status.NOT_FOUND, "No route plan found.");
        }
        if (job.error != null) {
            LOGGER.error("Exception during solving jobId ({}), message ({}).", jobId, job.error.getMessage(), job.error);
            throw new VehicleRoutingSolverException(jobId, job.error);
        }
        VehicleRoutingSolution routePlan = job.routePlan;
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        if (retrieve == Retrieve.STATUS) {
            return new VehicleRoutingSolution(routePlan.getName(), routePlan.getScore(), solverStatus);
        }
        routePlan.setSolverStatus(solverStatus);
        return routePlan;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{jobId}")
    public VehicleRoutingSolution terminateSolving(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId,
            @QueryParam("retrieve") Retrieve retrieve) {
        // TODO: Replace with .terminateEarlyAndWait(... [, timeout]); see https://github.com/TimefoldAI/timefold-solver/issues/77
        solverManager.terminateEarly(jobId);
        return getRoutePlan(jobId, retrieve);
    }

    public enum Retrieve {
        STATUS,
        FULL
    }

    private record Job(String jobId, VehicleRoutingSolution routePlan, Throwable error) {

        static Job newRoutePlan(VehicleRoutingSolution routePlan) {
            return new Job(null, routePlan, null);
        }

        static Job error(String jobId, Throwable error) {
            return new Job(jobId, null, error);
        }

    }
}
