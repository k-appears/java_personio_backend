package com.personio.api.hierarchy.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HierarchyTest {

    @Test
    void test_RecursivelyCreateHierarchy() {
        Map<String, Set<String>> mergedPaths = Map.of("Jonas", Set.of("Sophie"),
                "Sophie", Set.of("Nick"),
                "Nick", Set.of("Pete", "Barbara"));
        Hierarchy parent = new Hierarchy("Jonas", Set.of(), null);
        // {"Jonas":["Sophie"],"Nick":["Barbara","Pete"],"Sophie":["Nick"]}
        Hierarchy hierarchy = new Hierarchy("Jonas", Set.of("Sophie"), mergedPaths, parent);

        assertEquals("Jonas", hierarchy.getName());
        assertEquals(1, hierarchy.getEmployees().size());
        assertEquals(parent, hierarchy.getParent());
        Employee firstLevel = ((Employee) hierarchy.getEmployees().toArray()[0]);
        assertEquals("Sophie", firstLevel.getHierarchy().getName());
        assertEquals(hierarchy, firstLevel.getHierarchy().getParent());
        assertEquals(1, firstLevel.getHierarchy().getEmployees().size());
        Employee secondLevel = ((Employee) firstLevel.getHierarchy().getEmployees().toArray()[0]);
        assertEquals("Nick", secondLevel.getHierarchy().getName());
        assertEquals(firstLevel.getHierarchy().getName(), secondLevel.getHierarchy().getParent().getName());
        assertTrue(secondLevel.getHierarchy().getEmployees().stream().anyMatch(
                hierarchy1 -> hierarchy1.getHierarchy().getName().equals("Barbara")));
        assertTrue(secondLevel.getHierarchy().getEmployees().stream().anyMatch
                (hierarchy1 -> hierarchy1.getHierarchy().getName().equals("Pete")));
    }
}