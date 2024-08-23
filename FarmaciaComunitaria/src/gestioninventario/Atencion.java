/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestioninventario;

/**
 *
 * @author user1
 */
public class Atencion {
    public static void main(String[] args){
        Cliente carlos = new Cliente("Carlos", 20000);
        Medicamento paracetamol = new Medicamento("Paracetamol", "Analgesico", 10, 3800);
        
        carlos.realizarCompra(paracetamol);
    }
}
