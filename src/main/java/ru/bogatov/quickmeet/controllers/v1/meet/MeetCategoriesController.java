package ru.bogatov.quickmeet.controllers.v1.meet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.entities.MeetCategory;
import ru.bogatov.quickmeet.services.meet.MeetCategoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.MEET_MANAGEMENT + RouteConstants.CATEGORIES)
public class MeetCategoriesController {

    MeetCategoryService meetCategoryService;

    public MeetCategoriesController(MeetCategoryService meetCategoryService) {
        this.meetCategoryService = meetCategoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetCategory> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(meetCategoryService.findById(id));
    }

    @GetMapping()
    public ResponseEntity<List<MeetCategory>> getAllCategories(@RequestParam(required = false, defaultValue = "false") Boolean showHidden) {
        return ResponseEntity.ok(meetCategoryService.findAllByHidden(showHidden));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MeetCategory> updateCategoryById(@PathVariable UUID id, @RequestBody MeetCategory category) {
        return ResponseEntity.ok(meetCategoryService.updateCategory(id, category));
    }

    @PostMapping("")
    public ResponseEntity<MeetCategory> createCategory(@RequestBody MeetCategory category) {
        return ResponseEntity.ok(meetCategoryService.createCategory(category));
    }

}
