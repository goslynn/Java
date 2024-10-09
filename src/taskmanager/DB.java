// Creo que todo funciona correcto, lo unico que no se puede hacer es eliminar tasks, porque no existe query DELETE
// Probar crear un proyecto nuevo, una task nueva, asociar a un proyecto, guardar, asociar a otro proyecto guardar.

package taskmanager;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DB {
    private static final String url = "jdbc:postgresql://localhost:8000/taskmanager"; // Adjusted port for PostgreSQL default
    private static final String user = "postgres";
    private static final String password = "admin";
    private static Connection conn = null;

    // Open the connection to the database
    public static void connect() {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión exitosa a la base de datos.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Close the connection to the database
    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Fetch all tasks from the database
    public static void getTasks() {
        String query = "SELECT * FROM task ORDER BY id ASC";
        try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> packedTask = new HashMap<>();
                packedTask.put("id", rs.getInt("id"));

                // Manejo adecuado del valor NULL para project_id (clave foránea)
                Integer projectId = rs.getInt("project_id");
                if (rs.wasNull()) {
                    projectId = null; // Asignar null si el project_id en la base de datos era null
                }
                packedTask.put("project_id", projectId);

                packedTask.put("description", rs.getString("description"));
                packedTask.put("due_date", rs.getDate("due_date"));
                packedTask.put("status", rs.getBoolean("status"));

                // Manejo adecuado del valor NULL para priority
                Integer priority = rs.getInt("priority");
                if (rs.wasNull()) {
                    priority = null; // Asignar null si la prioridad en la base de datos era null
                }
                packedTask.put("priority", priority);

                // Pasar los valores a getTaskFromMap
                getTaskFromMap(packedTask);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch all projects from the database
    public static void getProjects() {
        String query = "SELECT * FROM project ORDER BY id ASC";
        try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Project pro = Project.createInstance(rs.getString("name"));
                pro.setIdProyecto(rs.getInt("id"));
                System.out.println("Project " + pro.getIdProyecto() + " parsed properly");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } //Call updateProjectTasks on execution

    // Delete record
    public static void deleteRecord(Integer taskId) {
        // Primero, obtener la tarea antes de eliminarla
        String query = "SELECT id, description, due_date, status, priority FROM task WHERE id = ?";
        try (PreparedStatement selectTaskStmt = conn.prepareStatement(query)) {
            selectTaskStmt.setInt(1, taskId);

            try (ResultSet rs = selectTaskStmt.executeQuery()) {
                if (rs.next()) {
                    // Obtener detalles de la tarea antes de eliminarla
                    Integer id = rs.getInt("id");
                    if (id <= 0){
                        return;
                    }
                    String description = rs.getString("description");
                    java.sql.Date dueDate = rs.getDate("due_date");
                    boolean status = rs.getBoolean("status");
                    Integer priority = rs.getInt("priority");

                    // Imprimir detalles de la tarea eliminada
                    System.out.println("Eliminando la Task con ID: " + id + ", Descripción: " + description + 
                                       ", Fecha límite: " + dueDate + ", Completada: " + status + ", Prioridad: " + priority);
                } else {
                    System.out.println("No se encontró ninguna Task con ID: " + taskId);
                    return; // Salir si no hay nada que eliminar
                }
            }

            // Luego, eliminar la tarea
            try (PreparedStatement deleteTaskStmt = conn.prepareStatement("DELETE FROM task WHERE id = ?")) {
                deleteTaskStmt.setInt(1, taskId);
                deleteTaskStmt.executeUpdate();
                System.out.println("Eliminación completada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Sync tasks and projects back to the database when closing the program
    public static void saveData(List<Task> tasks, List<Project> projects) {
        try {
            // Guardar nuevos proyectos (con IDs temporales negativos)
            String insertNewProjectQuery = "INSERT INTO project (name) VALUES (?) RETURNING id";
            String updateExistingProjectQuery = "UPDATE project SET name = ? WHERE id = ?";

            for (Project project : projects) {
                if (project.getIdProyecto() < 0) { // Proyectos nuevos con IDs temporales negativos
                    try (PreparedStatement stmt = conn.prepareStatement(insertNewProjectQuery, Statement.RETURN_GENERATED_KEYS)) {
                        // Insertar nuevo proyecto sin especificar el ID
                        stmt.setString(1, project.getNombreProyecto());
                        
                        stmt.executeUpdate();

                        // Obtener el ID generado por la base de datos
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                int newProjectId = rs.getInt(1); // Guardar el nuevo ID
                                System.out.println("Nuevo id para project " + project.getNombreProyecto() + " : " + newProjectId);

                                project.setIdProyecto(newProjectId); // Asignar el nuevo ID generado por la base de datos al proyecto

                                // Actualizar las tareas con el nuevo project_id
                                project.updateProjectTasks(); //para asegurar la task esta en la lista del project, deberia manejarse en el popup pero aca es mas facil.
                                for (Task task : project.getTasks()) {
                                    System.out.println("Actualizando parentProjectId de la task: " + task.getTaskId() + " con el nuevo project_id: " + newProjectId);
                                    task.setParentProjectId(newProjectId); // Actualizar el project_id en las tareas
                                }
                            }
                        } 
                    }
                } else { // Actualizar proyectos existentes
                    try (PreparedStatement stmt = conn.prepareStatement(updateExistingProjectQuery)) {
                        stmt.setString(1, project.getNombreProyecto());
                        stmt.setInt(2, project.getIdProyecto());

                        stmt.executeUpdate();
                    }
                }
            }

            // Guardar nuevas tareas (con IDs temporales negativos)
            String insertNewTaskQuery = "INSERT INTO task (project_id, description, due_date, status, priority) VALUES (?, ?, ?, ?, ?)";
            String updateExistingTaskQuery = "UPDATE task SET project_id = ?, description = ?, due_date = ?, status = ?, priority = ? WHERE id = ?";

            for (Task task : tasks) {
                System.out.println("Task ID: " + task.getTaskId() + ", Project ID: " + task.getParentProjectId());
                if (task.getTaskId() < 0) { // Tareas nuevas con IDs temporales negativos}
                    try (PreparedStatement stmt = conn.prepareStatement(insertNewTaskQuery, Statement.RETURN_GENERATED_KEYS)) {
                        // Insertar nueva tarea sin especificar el ID
                        if (task.getParentProjectId() != null) {
                            System.out.println("Usando project_id: " + task.getParentProjectId());
                            stmt.setInt(1, task.getParentProjectId());
                        } else {
                            System.out.println("project_id es null para Task ID: " + task.getTaskId());
                            stmt.setNull(1, java.sql.Types.INTEGER);
                        }
                        
                        stmt.setString(2, task.getDescripcion());
                        if (task.getFechaLimite() != null) {
                            stmt.setDate(3, java.sql.Date.valueOf(task.getFechaLimite()));
                        } else {
                            stmt.setNull(3, java.sql.Types.DATE);
                        }
                        stmt.setBoolean(4, task.isCompletada());
                        
                        if (task.getPrioridad() == null) {
                            stmt.setNull(5, java.sql.Types.INTEGER);
                        } else {
                            stmt.setInt(5, task.getIntPrioridad());
                        }

                        stmt.executeUpdate();

//                        // Obtener el ID generado por la base de datos
//                        try (ResultSet rs = stmt.getGeneratedKeys()) {
//                            if (rs.next()) {
//                                task.setTaskId(rs.getInt(1)); // Asignar el nuevo ID generado por la base de datos a la tarea
//                            }
//                        } //Un poco redundante porque el nuevo id ya se setea al cargar los datos, util solo si planeo ejecutar saveData() en tiempo de ejecucion
                    }
                } else { // Actualizar tareas existentes
                    try (PreparedStatement stmt = conn.prepareStatement(updateExistingTaskQuery)) {
                        if (task.getParentProjectId() == null) {
                            System.out.println("Parent Project ID is null, setting to NULL.");
                            stmt.setNull(1, java.sql.Types.INTEGER);
                        } else {
                            System.out.println("Parent Project ID: " + task.getParentProjectId());
                            stmt.setInt(1, task.getParentProjectId());
                        }

                        stmt.setString(2, task.getDescripcion());
                        System.out.println("Task description: " + task.getDescripcion());

                        if (task.getFechaLimite() == null) {
                            System.out.println("Due Date is null, setting to NULL.");
                            stmt.setNull(3, java.sql.Types.DATE);
                        } else {
                            System.out.println("Due Date: " + task.getFechaLimite());
                            stmt.setDate(3, java.sql.Date.valueOf(task.getFechaLimite()));
                        }

                        System.out.println("Task Completed Status: " + task.isCompletada());
                        stmt.setBoolean(4, task.isCompletada());

                        if (task.getPrioridad() == null) {
                            System.out.println("Task Priority is null, setting to NULL.");
                            stmt.setNull(5, java.sql.Types.INTEGER);
                        } else {
                            System.out.println("Task Priority: " + task.getIntPrioridad());
                            stmt.setInt(5, task.getIntPrioridad());
                        }

                        System.out.println("Task ID: " + task.getTaskId());
                        stmt.setInt(6, task.getTaskId());

                        stmt.executeUpdate();

                    }
                }
            }

            System.out.println("Los datos han sido sincronizados con éxito.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    
    public static void getTaskFromMap(Map<String,Object> packedTask) {
        Integer id =(Integer) packedTask.get("id");
        String description = (String) packedTask.get("description");
        Integer parentProjectFK = (Integer) packedTask.get("project_id");
        Integer priority = (Integer) packedTask.get("priority");
        boolean status = (boolean) packedTask.get("status");
        LocalDate dueDate = null; //por defecto null
        if (packedTask.get("due_date") != null) { //si la seleccion no es null entonces se parsea dueDate
            Date unparsedDueDate = (Date) packedTask.get("due_date");
            if (unparsedDueDate instanceof java.sql.Date) {
                // Convierte a LocalDate directamente si es un java.sql.Date
                dueDate = ((java.sql.Date) unparsedDueDate).toLocalDate();
            } else {
                // Si fuera otro tipo de Date (java.util.Date), manejarlo adecuadamente
                dueDate = unparsedDueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
        
        //INSTANCIANDO TASK
        Task task = Task.createInstance(description, dueDate, priority); 
        task.setTaskId(id);
        task.setCompletada(status);
        if (parentProjectFK != null) { //ya que el constructor no requiere proyecto se lo aplicamos por setter, en caso de que lo tenga
            task.setParentProjectId(parentProjectFK); //Si la FK no es null, se le asigna a la FK de la instancia
        } //solo se le pasa el id, el nombre habra que actualizarlo junto al listado de task perteneciente al proyecto
        System.out.println("Task " + task.getTaskId() + " parsed properly");
    }

}

