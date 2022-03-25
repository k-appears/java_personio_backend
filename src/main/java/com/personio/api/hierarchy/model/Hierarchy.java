package com.personio.api.hierarchy.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.*;
import java.util.stream.Collectors;

@DatabaseTable(tableName = "hierarchy")
public class Hierarchy {

    @DatabaseField(id = true)
    private String name;

    @DatabaseField(foreign = true)
    private Hierarchy parent;

    @ForeignCollectionField(foreignFieldName = "hierarchy")
    private Collection<Employee> employees;


    public Hierarchy() {
        // Required for ORM lite
    }

    public Hierarchy(String name, Set<Hierarchy> employees, Hierarchy parent) {
        this.name = name;
        this.parent = parent;
        this.employees = employees.stream().map(hierarchy -> new Employee(name, employees, parent)).collect(Collectors.toList());
    }

    public Hierarchy(String name, Set<String> employees, Map<String,
            Set<String>> mergedPaths, Hierarchy parent) {
        if (employees == null || employees.isEmpty()) {
            this.name = name;
            this.parent = parent;
            this.employees = Set.of();
        } else {
            Set<Employee> newEmployees = employees.stream()
                    .map(employee -> new Employee(employee, mergedPaths.get(employee), mergedPaths, this))
                    .collect(Collectors.toSet());
            this.name = name;
            this.parent = parent;
            this.employees = new ArrayList<>(newEmployees);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hierarchy hierarchy = (Hierarchy) o;
        return Objects.equals(name, hierarchy.name) &&
                Objects.equals(employees, hierarchy.employees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, employees);
    }

    public String getName() {
        return name;
    }

    public Set<Employee> getEmployees() {
        return new HashSet<>(employees);
    }

    public Hierarchy getParent() {
        return parent;
    }

    public String toExpectedJson() {
        return "{\"" + name + "\": " + getEmployeesStr() + "}";
    }

    @Override
    public String toString() {
        return "\"" + name + "\": " + getEmployeesStr() + "";
    }

    private String getEmployeesStr() {
        return employees.toString().replace("[", "{").replace("]", "}");
    }

}
