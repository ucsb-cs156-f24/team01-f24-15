package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

/**
 * This is a REST controller for HelpRequest
 */

@Tag(name = "HelpRequest")
@RequestMapping("/api/helprequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

    @Autowired
    HelpRequestRepository helpRequestRepository;

    /**
     * List all help requests
     * 
     * @return an iterable of HelpRequest
     */
    @Operation(summary = "List all help requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<HelpRequest> allHelpRequests() {
        Iterable<HelpRequest> helpRequests = helpRequestRepository.findAll();
        return helpRequests;
    }

    /**
     * Create a new help request
     * 
     * @param requesterEmail      the email of the requester
     * @param teamId              the team ID
     * @param tableOrBreakoutRoom the table or breakout room
     * @param requestTime         the request time
     * @param explanation         the explanation for the request
     * @param solved              whether the request is solved
     * @return the saved help request
     */
    @Operation(summary = "Create a new help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public HelpRequest postHelpRequest(
            @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name = "teamId") @RequestParam String teamId,
            @Parameter(name = "tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
            @Parameter(name="requestTime", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("requestTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime requestTime,
            @Parameter(name = "explanation") @RequestParam String explanation,
            @Parameter(name = "solved") @RequestParam boolean solved)
            throws JsonProcessingException {

        log.info("requestTime={}", requestTime);

        HelpRequest helpRequest = HelpRequest.builder()
                .requesterEmail(requesterEmail)
                .teamId(teamId)
                .tableOrBreakoutRoom(tableOrBreakoutRoom)
                .requestTime(requestTime)
                .explanation(explanation)
                .solved(solved)
                .build();

        HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);

        return savedHelpRequest;
    }
}
