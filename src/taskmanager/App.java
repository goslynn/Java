//Agregar eliminar project

package taskmanager;

import java.util.ArrayList;
import javax.swing.SwingUtilities;

//Add a removing functionality

public class App {
    public static ArrayList<Task> allTasks = new ArrayList<>();
    public static ArrayList<Task> pendingTasks = new ArrayList<>();
    public static ArrayList<Task> completedTasks = new ArrayList<>();
    public static ArrayList<Project> allProjects = new ArrayList<>();
    public static boolean flagAllTasksMutation; //notifica cambios en el listado global de Tasks
    
    //Flag
    public static void setflagAllTasksMutationON(){
        flagAllTasksMutation = true;
    }
    public static void setflagAllTasksMutationOFF(){
        flagAllTasksMutation = false;
    }
    
    public static void updatePendingTasks (){
        App.pendingTasks = new ArrayList<>();
        for (Task task : App.allTasks){
            if (!task.isCompletada()){
                App.pendingTasks.add(task);
            }
        }
    }
    
    public static void updateCompletedTasks (){
        App.completedTasks = new ArrayList<>();
        for (Task task : App.allTasks){
            if (task.isCompletada()){
                App.completedTasks.add(task);
            }
        }
    }
    
    public static void updateTasksListFromProjects(){
        for (Project pro : App.allProjects){
            pro.updateProjectTasks();
        }
    }
    
    public static void shutdown() {
        //DB.saveData(allTasks, allProjects);
        //DB.closeConnection();
    }
    
    public static void main(String[] args) {
        //DB.connect();
        SwingUtilities.invokeLater(() -> {
        //DB.getProjects();
        //DB.getTasks();
        updateTasksListFromProjects();
        new TaskManagerGUI().setVisible(true);
        });
    }

    
}
