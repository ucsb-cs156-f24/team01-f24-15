package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

    @MockBean
    HelpRequestRepository helpRequestRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/helprequest/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/helprequest/all"))
                .andExpect(status().is(403)); // logged out users can't access this endpoint
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/helprequest/all"))
                .andExpect(status().isOk()); // logged in users can access this endpoint
    }

    // Tests for GET /api/helprequest/all

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_helprequest() throws Exception {

        // arrange
        LocalDateTime requestTime1 = LocalDateTime.parse("2024-10-22T18:11:56");
        LocalDateTime requestTime2 = LocalDateTime.parse("2024-10-23T09:30:00");

        HelpRequest helpRequest1 = HelpRequest.builder()
                .requesterEmail("user1@example.com")
                .teamId("team01")
                .tableOrBreakoutRoom("Table 1")
                .requestTime(requestTime1)
                .explanation("Need help with setup")
                .solved(false)
                .build();

        HelpRequest helpRequest2 = HelpRequest.builder()
                .requesterEmail("user2@example.com")
                .teamId("team02")
                .tableOrBreakoutRoom("Table 2")
                .requestTime(requestTime2)
                .explanation("Need help with deployment")
                .solved(true)
                .build();

        ArrayList<HelpRequest> expectedRequests = new ArrayList<>();
        expectedRequests.addAll(Arrays.asList(helpRequest1, helpRequest2));

        when(helpRequestRepository.findAll()).thenReturn(expectedRequests);

        // act
        MvcResult response = mockMvc.perform(get("/api/helprequest/all"))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(helpRequestRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedRequests);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Authorization tests for /api/helprequest/post

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/helprequest/post"))
                .andExpect(status().is(403)); // logged out users cannot post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/helprequest/post"))
                .andExpect(status().is(403)); // only admins can post
    }

    // Tests for POST /api/helprequest/post

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_user_can_post_a_new_helprequest() throws Exception {
        // arrange
        LocalDateTime requestTime = LocalDateTime.parse("2024-10-22T18:11:56");

        HelpRequest helpRequest = HelpRequest.builder()
                .requesterEmail("admin@example.com")
                .teamId("adminTeam")
                .tableOrBreakoutRoom("Breakout Room 1")
                .requestTime(requestTime)
                .explanation("Urgent help needed")
                .solved(false)
                .build();

                when(helpRequestRepository.save(eq(helpRequest))).thenReturn(helpRequest);
        
                // act
                MvcResult response = mockMvc.perform(
                        post("/api/helprequest/post")
                                .param("requesterEmail", "admin@example.com")
                                .param("teamId", "adminTeam")
                                .param("tableOrBreakoutRoom", "Breakout Room 1")
                                .param("requestTime", "2024-10-22T18:11:56")
                                .param("explanation", "Urgent help needed")
                                .param("solved", "false")
                                .with(csrf()))
                        .andExpect(status().isOk()).andReturn();
        
                // assert
                verify(helpRequestRepository, times(1)).save(helpRequest);
                String expectedJson = mapper.writeValueAsString(helpRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
            }
        }
