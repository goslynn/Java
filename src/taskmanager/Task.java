package taskmanager;

import java.time.*;
import java.util.Objects;


public class Task {
    private Integer taskId;
    private String descripcion;
    private LocalDate fechaLimite;
    private priorityRange prioridad; //High - Medium - Low | 2 - 1 - 0
    private boolean completada;
    private Integer parentProjectId;
    private String projectName;

    
    public static enum priorityRange {
        LOW(0), 
        MEDIUM(1),
        HIGH(2);
        
        //Sets valor int asociado a cada enum
        private final int value;

        priorityRange(int value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
        
        public static priorityRange fromInt(Integer value) {
            if (value == null){
                return null;
            }
            for (priorityRange pr : priorityRange.values()) {
                if (Objects.equals(pr.getValue(), value)) {
                    return pr;
                }
            }
            throw new IllegalArgumentException("Valor no válido: " + value);
        }
    }
    
    private Task(String descripcion, LocalDate fechaLimite, priorityRange prioridad) {
        this.descripcion = descripcion;
        this.fechaLimite = fechaLimite;
        this.prioridad = prioridad;
        this.completada = false;
        this.projectName = null;
    }

    // Unificado método createInstance
    public static Task createInstance(String descripcion, LocalDate fechaLimite, Object prioridad) {
        Task instance;
        if (prioridad instanceof priorityRange) { //si es instancia del tipo enum usar constructor normal
            instance = new Task(descripcion, fechaLimite, (priorityRange) prioridad);
        } else if (prioridad instanceof Integer) { //si es int utilizar metodo del enum para obtener desde un int
            instance = new Task(descripcion, fechaLimite, priorityRange.fromInt((Integer) prioridad));
        } else if (prioridad == null){
            instance = new Task(descripcion, fechaLimite, null);
        } 
        else {
            throw new IllegalArgumentException("Prioridad no válida. Debe ser priorityRange o int.");
        }
        
        App.allTasks.add(instance); //Agregar al listado
        return instance; 
    }

    // getters

    public Integer getTaskId() { return taskId; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public priorityRange getPrioridad() { return prioridad; }
    public Integer getIntPrioridad() { return prioridad.getValue(); }
    public boolean isCompletada() { return completada; }
    public Integer getParentProjectId() { return parentProjectId; }
    public String getProjectName() { return projectName; }

    // setters
    
    public void setCompletada(boolean completada) {this.completada = completada;}
    public void setParentProjectId(Integer parentProjectId) {this.parentProjectId = parentProjectId;}
    public void setProjectName(String projectName) {this.projectName = projectName;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFechaLimite(LocalDate fechaLimite) {this.fechaLimite = fechaLimite;}
    public void setPrioridad(priorityRange prioridad) {this.prioridad = prioridad;} //enum
    public void setPrioridadFromInt(Integer value) {this.prioridad = priorityRange.fromInt(value);}
    public void setTaskId(Integer taskId) {this.taskId = taskId;}
    
    
    


    //Method for adding tasks with createInstance;
    
    
    
    @Override
    public String toString() {
        return "\nTask{" + "\ntaskId : " + taskId + "\nProject_id_FK : " + parentProjectId +  "\ndescripcion : " + descripcion + "\nfechaLimite : " + fechaLimite + "\nprioridad : " + prioridad + "\ncompletada : " + completada + "\n}";
    }
    
    
    
}
