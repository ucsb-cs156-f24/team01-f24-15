package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)

public class UCSBOrganizationControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationRepository ucsbOrganizationRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsborganization/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

                // arrange

                UCSBOrganization dsp = UCSBOrganization.builder()
                        .orgCode("DSP")
                        .orgTranslationShort("DELTA SIG")
                        .orgTranslation("DELTA SIGMA PI")
                        .inactive(false)
                        .build();

                UCSBOrganization axo = UCSBOrganization.builder()
                        .orgCode("AXO")
                        .orgTranslationShort("ACHIO")
                        .orgTranslation("ALPHA CHI OMEGA SORORITY")
                        .inactive(true)
                        .build();

                ArrayList<UCSBOrganization> expectedOrgs = new ArrayList<>();
                expectedOrgs.addAll(Arrays.asList(dsp, axo));

                when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrgs);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrgs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }


        // Authorization tests for /api/ucsborganization/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_org() throws Exception {
                // arrange

                UCSBOrganization rok = UCSBOrganization.builder()
                        .orgCode("ROK")
                        .orgTranslationShort("CLIMBING")
                        .orgTranslation("UCSBCLIMBING")
                        .inactive(true)
                        .build();

                when(ucsbOrganizationRepository.save(eq(rok))).thenReturn(rok);

                // act
                MvcResult response = mockMvc.perform(
                post("/api/ucsborganization/post?orgCode=ROK&orgTranslationShort=CLIMBING&orgTranslation=UCSBCLIMBING&inactive=true")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).save(rok);
                String expectedJson = mapper.writeValueAsString(rok);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_org() throws Exception {
                // arrange

                UCSBOrganization ind = UCSBOrganization.builder()
                        .orgCode("IND")
                        .orgTranslationShort("INDUS")
                        .orgTranslation("INDUS SOUTH ASIAN CLUB")
                        .inactive(true)
                        .build();
                when(ucsbOrganizationRepository.findById(eq("IND"))).thenReturn(Optional.of(ind));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=IND")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("IND");
                verify(ucsbOrganizationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id IND deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_org_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationRepository.findById(eq("tedex"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=tedex")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("tedex");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id tedex not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
	public void admin_can_edit_an_existing_organization() throws Exception {
		// arrange

		UCSBOrganization ksigOrig = UCSBOrganization.builder()
                        .orgCode("KSIG")
                        .orgTranslationShort("KAPPA SIGMA")
                        .orgTranslation("KAPPA SIGMA FRATERNITY")
                        .inactive(true)
                        .build();
		
		UCSBOrganization ksigEdited = UCSBOrganization.builder()
                        .orgCode("ksig")
                        .orgTranslationShort("KAPPA SIGMA FRAT")
                        .orgTranslation("KAPPA SIGMA SOCIAL FRATERNITY")
                        .inactive(false)
                        .build();

		String requestBody = mapper.writeValueAsString(ksigEdited);

		when(ucsbOrganizationRepository.findById(eq("KSIG"))).thenReturn(Optional.of(ksigOrig));

		// act
		MvcResult response = mockMvc.perform(
						put("/api/ucsborganization?orgCode=KSIG")
										.contentType(MediaType.APPLICATION_JSON)
										.characterEncoding("utf-8")
										.content(requestBody)
										.with(csrf()))
						.andExpect(status().isOk()).andReturn();

		// assert
		verify(ucsbOrganizationRepository, times(1)).findById("KSIG");
		verify(ucsbOrganizationRepository, times(1)).save(ksigEdited); // should be saved with updated info
		String responseString = response.getResponse().getContentAsString();
		assertEquals(requestBody, responseString);
	}

	@WithMockUser(roles = { "ADMIN", "USER" })
	@Test
	public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
		// arrange

		UCSBOrganization zprEdited = UCSBOrganization.builder()
							.orgCode("ATO")
							.orgTranslationShort("ALPHA TAU OMEGA")
							.orgTranslation("ALPHA TAU OMEGA FRATERNITY")
							.inactive(true)
							.build();

		String requestBody = mapper.writeValueAsString(zprEdited);

		when(ucsbOrganizationRepository.findById(eq("ATO"))).thenReturn(Optional.empty());

		// act
		MvcResult response = mockMvc.perform(
						put("/api/ucsborganization?orgCode=ATO")
										.contentType(MediaType.APPLICATION_JSON)
										.characterEncoding("utf-8")
										.content(requestBody)
										.with(csrf()))
						.andExpect(status().isNotFound()).andReturn();

		// assert
		verify(ucsbOrganizationRepository, times(1)).findById("ATO");
		Map<String, Object> json = responseToJson(response);
		assertEquals("UCSBOrganization with id ATO not found", json.get("message"));
	}
        
        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganization?orgCode=dps"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
                // arrange
                UCSBOrganization axo = UCSBOrganization.builder()
                        .orgCode("AXO")
                        .orgTranslationShort("ACHIO")
                        .orgTranslation("ALPHA CHI OMEGA SORORITY")
                        .inactive(true)
                        .build();
                when(ucsbOrganizationRepository.findById(eq("AXO"))).thenReturn(Optional.of(axo));
                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=AXO"))
                                .andExpect(status().isOk()).andReturn();
                // assert
                verify(ucsbOrganizationRepository, times(1)).findById(eq("AXO"));
                String expectedJson = mapper.writeValueAsString(axo);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
                // arrange
                when(ucsbOrganizationRepository.findById(eq("ATO"))).thenReturn(Optional.empty());
                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=ATO"))
                                .andExpect(status().isNotFound()).andReturn();
                // assert
                verify(ucsbOrganizationRepository, times(1)).findById(eq("ATO"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganization with id ATO not found", json.get("message"));
        }

}