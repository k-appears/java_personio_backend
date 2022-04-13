package com.personio.api.hierarchy.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Map;
import java.util.Set;


@DatabaseTable(tableName = "employee")
public class Employee {

    @DatabaseField(generatedId = true)
    Integer id;

    @DatabaseField(foreign = true)
    private Hierarchy hierarchy;

    public Employee() {
        // Required for ORM lite
    }

    public Employee(String supervisor, Set<String> employees, Map<String, Set<String>> mergedPaths, Hierarchy parent) {
        this.hierarchy = new Hierarchy(supervisor, employees, mergedPaths, parent);
    }

    public Employee(String supervisor, Set<Hierarchy> employees, Hierarchy parent) {
        this.hierarchy = new Hierarchy(supervisor, employees, parent);
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    @Override
    public String toString() {
        return hierarchy.toString();
    }

}
