package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * This is a REST controller for UCSBDiningCommonsMenuItem
 */

@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

    @Autowired
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    /**
     * List all UCSB Dining Commons Menu Items
     * 
     * @return an iterable of UCSBDiningCommonsMenuItem
     */
    @Operation(summary= "List all Dining Commons Menu Items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItem() {
        Iterable<UCSBDiningCommonsMenuItem> ucsbdiningcommonmenuitem = ucsbDiningCommonsMenuItemRepository.findAll();
        return ucsbdiningcommonmenuitem;
    }

    /**
     * Get a menu item by id
     * 
     * @param id the id of the menu item
     * @return a UCSBDiningCommonMenuItem
     */
    @Operation(summary= "Get a menu item")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenuItem getById(
            @Parameter(name="id") @RequestParam Long id) {
                UCSBDiningCommonsMenuItem ucsbdiningcommonmenuitem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        return ucsbdiningcommonmenuitem;
    }

    /**
     * This method creates a new diningcommons. Accessible only to users with the role "ROLE_ADMIN".
     * @param diningCommonsCode code of the diningcommonsmenuitem
     * @param name name of the diningcommonsmenuitem
     * @param station station of the diningcommonsmenuitem
     * @return the saved diningcommonsmenuitem
     */
    @Operation(summary= "Create a new menu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenuItem postItem(
        @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode,
        @Parameter(name="name") @RequestParam String name,
        @Parameter(name="station") @RequestParam String station
        )
        {
        UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();
        item.setDiningCommonsCode(diningCommonsCode);
        item.setName(name);
        item.setStation(station);

        UCSBDiningCommonsMenuItem savedItem = ucsbDiningCommonsMenuItemRepository.save(item);

        return savedItem;
    }


    /**
     * Delete a UCSBDiningCommonsMenuItem
     * 
     * @param id the id of the item to delete
     * @return a message indicating the date was deleted
     */
    /**
    @Operation(summary= "Delete a UCSBDiningCommonsMenuItem")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBDiningCommonsMenuItem(
            @Parameter(name="id") @RequestParam Long id) {
                UCSBDiningCommonsMenuItem ucsbdiningcommonmenuitem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

                ucsbDiningCommonsMenuItemRepository.delete(ucsbdiningcommonmenuitem);
        return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
    }
    */

    /**
     * Update a single item
     * 
     * @param id id of the item to update
     * @param incoming the new item
     * @return the updated item object
     */
    /**
    @Operation(summary= "Update a single item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

                UCSBDiningCommonsMenuItem ucsbdiningcommonmenuitem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

                ucsbdiningcommonmenuitem.setDiningCommonsCode(incoming.getDiningCommonsCode());
                ucsbdiningcommonmenuitem.setName(incoming.getName());
                ucsbdiningcommonmenuitem.setStation(incoming.getStation());

        ucsbDiningCommonsMenuItemRepository.save(ucsbdiningcommonmenuitem);

        return ucsbdiningcommonmenuitem;
    }
    */
}
