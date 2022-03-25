package com.personio.api.hierarchy.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;


@DatabaseTable(tableName = "employee")
public class Employee {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(hierarchy, employee.hierarchy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hierarchy);
    }
}
