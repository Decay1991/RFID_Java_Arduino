/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pkgGestaoRFID;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JOptionPane;

public class ControlePortaLigaLed {
  private OutputStream serialOut;
  private int taxa;
  private String portaCOM;
  
  private InputStream serialIn;
  
  //criar o buffer para depois ler
  //para depois lermos deste buffer a informacao

  /**
   * Construtor da classe ControlePorta
   * @param portaCOM - Porta COM que será utilizada para enviar os dados para o arduino
   * @param taxa - Taxa de transferência da porta serial geralmente é 9600
   */
  public ControlePortaLigaLed(String portaCOM, int taxa) {
    this.portaCOM = portaCOM;
    this.taxa = taxa;
    this.initialize();
  }     
 
  /**
   * Médoto que verifica se a comunicação com a porta serial está ok
   */
  private void initialize() {
    try {
      //Define uma variável portId do tipo CommPortIdentifier para realizar a comunicação serial
      CommPortIdentifier portId = null;
      try {
        //Tenta verificar se a porta COM informada existe
        portId = CommPortIdentifier.getPortIdentifier(this.portaCOM);
      }catch (NoSuchPortException npe) {
        //Caso a porta COM não exista será exibido um erro 
        JOptionPane.showMessageDialog(null, "Porta COM não encontrada.", "Porta COM", JOptionPane.PLAIN_MESSAGE);
      }
      //Abre a porta COM 
      SerialPort port = (SerialPort) portId.open("Comunicação serial", this.taxa);
      serialOut = port.getOutputStream();
      port.setSerialPortParams(this.taxa, //taxa de transferência da porta serial 
                               SerialPort.DATABITS_8, //taxa de 10 bits 8 (envio)
                               SerialPort.STOPBITS_1, //taxa de 10 bits 1 (recebimento)
                               SerialPort.PARITY_NONE); //receber e enviar dados
    }catch (Exception e) {
      e.printStackTrace();
    }
}

  /**
   * Método que fecha a comunicação com a porta serial
   */
  public void close() {
    try {
        serialOut.close();
    }catch (IOException e) {
      JOptionPane.showMessageDialog(null, "Não foi possível fechar porta COM.", "Fechar porta COM", JOptionPane.PLAIN_MESSAGE);
    }
  }

  /**
   * @param opcao - Valor a ser enviado pela porta serial
   */
  public void enviaDados(int opcao){
    try {
      serialOut.write(opcao);//escreve o valor na porta serial para ser enviado
        System.out.println("serialOut.write("+opcao+")");
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, "Não foi possível enviar o dado. ", "Enviar dados", JOptionPane.PLAIN_MESSAGE);
        System.out.println("DADOS A ENVIAR : " + opcao);
    }
  }
  
//    public void leDados() {
//
//        try {
//            byte[] readBuffer = new byte[140];
//            //while (serialIn.available() > 0) {
//                int numBytes = serialIn.read(readBuffer);
//                String result = new String(readBuffer, 0, numBytes);
//                System.out.print(result);
//                                
//            //}
//
//        } catch (IOException e) {
//        }
//    }
    
    public String lerDados(){
        String result = "";
        try {
            byte[] readBuffer = new byte[140];
            
            //while (serialIn.available() > 0) {
                int numBytes = serialIn.read(readBuffer);
                result = new String(readBuffer, 0, numBytes);
                System.out.print("resultadoDentroFuncao:" + result);
                                
           // }

        } catch (IOException e) {
        }
        
       return result;
    }
  
}
