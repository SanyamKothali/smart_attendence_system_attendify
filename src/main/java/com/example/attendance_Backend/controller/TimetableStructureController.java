package com.example.attendance_Backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance_Backend.model.TimetableStructure;
import com.example.attendance_Backend.service.TimetableStructureService;

/**
 * Admin manages the timetable structure (period/break rows).
 */
@RestController
@RequestMapping("/api/admin/timetable/structure")
@CrossOrigin(origins = "*")
public class TimetableStructureController {

    private final TimetableStructureService service;

    public TimetableStructureController(TimetableStructureService service) {
        this.service = service;
    }

    /** GET all rows ordered by slot_order */
    @GetMapping
    public ResponseEntity<List<TimetableStructure>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /** POST — add a new period or break row */
    @PostMapping
    public ResponseEntity<TimetableStructure> add(@RequestBody TimetableStructure structure) {
        return ResponseEntity.ok(service.save(structure));
    }

    /** PUT — update an existing row */
    @PutMapping("/{id}")
    public ResponseEntity<TimetableStructure> update(
            @PathVariable Integer id,
            @RequestBody TimetableStructure updated) {
        return ResponseEntity.ok(service.update(id, updated));
    }

    /** DELETE — remove a row */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
