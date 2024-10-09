
package taskmanager;

import java.util.ArrayList;
import java.time.*;
import java.util.Iterator;
import java.util.Objects;
import taskmanager.Task.priorityRange;


public class Project {
    private Integer idProyecto;
    private String nombreProyecto;
    private ArrayList<Task> tasks = new ArrayList<>();

    private Project(String nombreProyecto) { //Mismo tema que en task
        this.nombreProyecto = nombreProyecto;
    }

    public static Project createInstance(String nombreProyecto){
        Project instance = new Project(nombreProyecto);
        App.allProjects.add(instance);
        return instance;
    }
    
    //Getters
    public ArrayList<Task> getTasks() {
        return tasks;
    }
    
    public Integer getIdProyecto() {
        return idProyecto;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public Task getTask(int id){
        for(Task task : tasks){
            if(task.getTaskId() == id){
                return task;
            }
        }
        return null;
    }

    //Setter
    public void setIdProyecto(Integer idProyecto) {
        this.idProyecto = idProyecto;
    }
    
    public void createTask(String description, LocalDate fechaLimite, priorityRange prioridad){
        Task task = Task.createInstance(description, fechaLimite, prioridad); //Este metodo constructor custom añade a AllTasks automaticamente
        task.setParentProjectId(idProyecto);
        task.setProjectName(nombreProyecto);
        tasks.add(task);
    }
    
    public void addTask(Task task){ //Añadir manual
        task.setParentProjectId(idProyecto);
        task.setProjectName(nombreProyecto);
        this.tasks.add(task);
    }
    
    

    public void updateProjectTasks() {
        Iterator<Task> iterator = this.tasks.iterator();

        // Eliminar tasks cuyo id ya no coincida (tasks editadas y removidas del proyecto)
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.getProjectName() == null || !Objects.equals(task.getParentProjectId(), idProyecto)) {
                iterator.remove();
            }
        }

        // Actualizar la lista de tareas del proyecto
        for (Task task : App.allTasks) {
            if (!this.tasks.contains(task)) { //Si la lista de tasks de este proyects no contiene la task que se esta iterando
                if (task.getParentProjectId() == this.idProyecto){ //si el id coincide agregar a la lista
                    this.addTask(task);
                }
            }
        }
    }

    
    public void showTasks(){ //Commandline
        System.out.println("Proyecto ->" + this.nombreProyecto + "\n"
                + "=================================");
        for(Task task : tasks){
            System.out.println("\t" + task.toString());
        }
    }
    
}
