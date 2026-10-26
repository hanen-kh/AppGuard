package com.sfm.projetPFE.ServiceStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceMetricsDTO {
    private Fields fields;
    private Metricset metricset;
    private SystemInfo system;

    // Getters + setters

    public Fields getFields() { return fields; }
    public void setFields(Fields fields) { this.fields = fields; }

    public Metricset getMetricset() { return metricset; }
    public void setMetricset(Metricset metricset) { this.metricset = metricset; }

    public SystemInfo getSystem() { return system; }
    public void setSystem(SystemInfo system) { this.system = system; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        private String serviceName;
        private Boolean isActive;
        private Integer port;

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public Boolean isActive() { return isActive; }
        public void setIsActive(Boolean active) { isActive = active; }

        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metricset {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SystemInfo {
        private Process process;

        public Process getProcess() { return process; }
        public void setProcess(Process process) { this.process = process; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Process {
            private Cpu cpu;
            private Memory memory;

            public Cpu getCpu() { return cpu; }
            public void setCpu(Cpu cpu) { this.cpu = cpu; }

            public Memory getMemory() { return memory; }
            public void setMemory(Memory memory) { this.memory = memory; }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Cpu {
                private Double pct;
                public Double getPct() { return pct; }
                public void setPct(Double pct) { this.pct = pct; }
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Memory {
                private Double pct;
                public Double getPct() { return pct; }
                public void setPct(Double pct) { this.pct = pct; }
            }
        }
    }
}
