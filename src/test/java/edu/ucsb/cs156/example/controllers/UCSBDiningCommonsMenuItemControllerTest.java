package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTest extends ControllerTestCase {

    @MockBean
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsbdiningcommonmenuitem/all"))
                .andExpect(status().is(403)); // Forbidden for logged-out users
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsbdiningcommonmenuitem/all"))
                .andExpect(status().isOk()); // Success for logged-in users
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsbdiningcommonmenuitem/post")
                        .param("diningCommonsCode", "test-code")
                        .param("name", "Test Item")
                        .param("station", "Test Station")
                        .with(csrf()))  // CSRF token is needed for POST
                .andExpect(status().is(403)); // Now it will check for forbidden access correctly
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsbdiningcommonmenuitem/post")
                        .param("diningCommonsCode", "test-code")
                        .param("name", "Test Item")
                        .param("station", "Test Station")
                        .with(csrf()))  // CSRF token is needed for POST
                .andExpect(status().is(403)); // Now it will check for forbidden access correctly
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_post_a_new_menu_item() throws Exception {
        // Arrange
        UCSBDiningCommonsMenuItem item = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("test-code")
                .name("Test Item")
                .station("Test Station")
                .build();

        when(ucsbDiningCommonsMenuItemRepository.save(any())).thenReturn(item);

        // Act
        MvcResult response = mockMvc.perform(
                post("/api/ucsbdiningcommonmenuitem/post")
                        .param("diningCommonsCode", "test-code")
                        .param("name", "Test Item")
                        .param("station", "Test Station")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // Assert
        ArgumentCaptor<UCSBDiningCommonsMenuItem> captor =
                ArgumentCaptor.forClass(UCSBDiningCommonsMenuItem.class);
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(captor.capture());

        UCSBDiningCommonsMenuItem savedItem = captor.getValue();
        assertEquals("test-code", savedItem.getDiningCommonsCode());
        assertEquals("Test Item", savedItem.getName());
        assertEquals("Test Station", savedItem.getStation());

        String responseString = response.getResponse().getContentAsString();
        UCSBDiningCommonsMenuItem returnedItem = mapper.readValue(responseString, UCSBDiningCommonsMenuItem.class);

        // Check the response matches the saved item
        assertEquals("test-code", returnedItem.getDiningCommonsCode());
        assertEquals("Test Item", returnedItem.getName());
        assertEquals("Test Station", returnedItem.getStation());
    }

    

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_menu_items() throws Exception {
        // Arrange
        UCSBDiningCommonsMenuItem item1 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("code1")
                .name("Item 1")
                .station("Station 1")
                .build();

        UCSBDiningCommonsMenuItem item2 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("code2")
                .name("Item 2")
                .station("Station 2")
                .build();

        ArrayList<UCSBDiningCommonsMenuItem> items = new ArrayList<>(Arrays.asList(item1, item2));

        when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(items);

        // Act
        MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonmenuitem/all"))
                .andExpect(status().isOk()).andReturn();

        // Assert
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(items);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_get_menu_item_by_id_exists() throws Exception {
        // Arrange
        UCSBDiningCommonsMenuItem item = UCSBDiningCommonsMenuItem.builder()
                .id(1L)
                .diningCommonsCode("test-code")
                .name("Test Item")
                .station("Test Station")
                .build();

        when(ucsbDiningCommonsMenuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act
        MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonmenuitem")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);

        String responseString = response.getResponse().getContentAsString();
        UCSBDiningCommonsMenuItem returnedItem = mapper.readValue(responseString, UCSBDiningCommonsMenuItem.class);

        assertEquals("test-code", returnedItem.getDiningCommonsCode());
        assertEquals("Test Item", returnedItem.getName());
        assertEquals("Test Station", returnedItem.getStation());
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_get_menu_item_by_id_not_found() throws Exception {
        // Arrange
        when(ucsbDiningCommonsMenuItemRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonmenuitem")
                        .param("id", "99"))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(99L);

        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("UCSBDiningCommonsMenuItem with id 99 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_update_an_existing_menu_item() throws Exception {
        // Arrange
        UCSBDiningCommonsMenuItem originalItem = UCSBDiningCommonsMenuItem.builder()
                .id(1L)
                .diningCommonsCode("code1")
                .name("Original Item")
                .station("Original Station")
                .build();

        UCSBDiningCommonsMenuItem updatedItem = UCSBDiningCommonsMenuItem.builder()
                .id(1L)
                .diningCommonsCode("code2")
                .name("Updated Item")
                .station("Updated Station")
                .build();

        String requestBody = mapper.writeValueAsString(updatedItem);

        when(ucsbDiningCommonsMenuItemRepository.findById(1L)).thenReturn(Optional.of(originalItem));
        when(ucsbDiningCommonsMenuItemRepository.save(any())).thenReturn(updatedItem);

        // Act
        MvcResult response = mockMvc.perform(
                put("/api/ucsbdiningcommonmenuitem")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);

        // Capture and verify saved item fields
        ArgumentCaptor<UCSBDiningCommonsMenuItem> captor = ArgumentCaptor.forClass(UCSBDiningCommonsMenuItem.class);
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(captor.capture());

        UCSBDiningCommonsMenuItem savedItem = captor.getValue();
        assertEquals("code2", savedItem.getDiningCommonsCode());
        assertEquals("Updated Item", savedItem.getName());
        assertEquals("Updated Station", savedItem.getStation());

        // Verify the response
        String responseString = response.getResponse().getContentAsString();
        assertNotNull(responseString);
        UCSBDiningCommonsMenuItem returnedItem = mapper.readValue(responseString, UCSBDiningCommonsMenuItem.class);
        assertNotNull(returnedItem);
        assertEquals("code2", returnedItem.getDiningCommonsCode());
        assertEquals("Updated Item", returnedItem.getName());
        assertEquals("Updated Station", returnedItem.getStation());
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_update_nonexistent_menu_item() throws Exception {
        // Arrange
        Long id = 99L;
        UCSBDiningCommonsMenuItem updatedItem = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("code2")
                .name("Updated Item")
                .station("Updated Station")
                .build();

        String requestBody = mapper.writeValueAsString(updatedItem);

        when(ucsbDiningCommonsMenuItemRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        MvcResult response = mockMvc.perform(
                put("/api/ucsbdiningcommonmenuitem")
                        .param("id", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(id);

        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("UCSBDiningCommonsMenuItem with id " + id + " not found", json.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void regular_user_cannot_update_menu_item() throws Exception {
        // Arrange
        UCSBDiningCommonsMenuItem updatedItem = UCSBDiningCommonsMenuItem.builder()
                .id(1L)
                .diningCommonsCode("code2")
                .name("Updated Item")
                .station("Updated Station")
                .build();

        String requestBody = mapper.writeValueAsString(updatedItem);

        // Act
        mockMvc.perform(
                put("/api/ucsbdiningcommonmenuitem")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isForbidden()); // 403 Forbidden
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_an_existing_menu_item() throws Exception {
        // Arrange
        UCSBDiningCommonsMenuItem item = UCSBDiningCommonsMenuItem.builder()
                .id(1L)
                .diningCommonsCode("code1")
                .name("Item 1")
                .station("Station 1")
                .build();

        when(ucsbDiningCommonsMenuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act
        MvcResult response = mockMvc.perform(
                delete("/api/ucsbdiningcommonmenuitem")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(item);

        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBDiningCommonsMenuItem with id 1 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_delete_nonexistent_menu_item() throws Exception {
        // Arrange
        when(ucsbDiningCommonsMenuItemRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        MvcResult response = mockMvc.perform(
                delete("/api/ucsbdiningcommonmenuitem")
                        .param("id", "99")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(99L);

        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("UCSBDiningCommonsMenuItem with id 99 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void regular_user_cannot_delete_menu_item() throws Exception {
        // Act
        mockMvc.perform(
                delete("/api/ucsbdiningcommonmenuitem")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden()); // 403 Forbidden
        }

}
