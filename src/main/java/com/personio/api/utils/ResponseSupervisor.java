package com.personio.api.utils;

import com.google.gson.annotations.Expose;

import java.util.Objects;

public class ResponseSupervisor {

    @Expose
    private final String supervisor;

    @Expose
    private final String supervisorOfSupervisor;

    public ResponseSupervisor(String supervisor, String supervisorOfSupervisor) {
        this.supervisor = supervisor;
        this.supervisorOfSupervisor = supervisorOfSupervisor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseSupervisor that = (ResponseSupervisor) o;
        return Objects.equals(supervisor, that.supervisor) && Objects.equals(supervisorOfSupervisor, that.supervisorOfSupervisor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supervisor, supervisorOfSupervisor);
    }
}
