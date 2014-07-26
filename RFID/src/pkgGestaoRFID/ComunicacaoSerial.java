/**
 * @authores BrUnO & rAfAeL *
 */
package pkgGestaoRFID;

import gnu.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JOptionPane;

public class ComunicacaoSerial implements SerialPortEventListener {

    //variáveis globais
    InterfaceDeGestao ig = new InterfaceDeGestao();
    public SerialPort serialPort;
    public String strGlobal = "";
    private InputStream input; // Buffered input stream from the port
    private OutputStream output; // The output stream to the port
    private static final int TIME_OUT = 2000; // Milliseconds to block while waiting for port open
    private static final int DATA_RATE = 19200;
    public String enviaRFID = "";
    public String inputBuffer = "";

    private byte[] teste;

    /*String[] data =  new String[5];*/
    int contador = 0;

    ArrayList teste2 = new ArrayList();
    ArrayList rfid_arrayList = new ArrayList();

    /* DADOS LIGAR ARDUINO */
    private String COM = "COM10";
    //private int BAUND = 19200;
    private static final String PORT_NAMES[] = {"/dev/tty.usbserial-A9007UX1", "/dev/ttyUSB0", "COM10"};

    //VARIAVIES PARA MOSTRAR RFID
    String estado = "ESPERAFF";

    //GUARDA O RFID FINAL
    String rfid_final = "";

    //GUARDAR O VALOR DA MAQUINA DE ESTADOS
    String maquinaDeEstados = "";

    public static void main(String[] args) {
        ComunicacaoSerial cs = new ComunicacaoSerial();
        cs.initialize();
    }

    public void initialize() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        // iterate through, looking for the port
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }

        if (portId == null) {
            System.out.println("A porta COM não foi encontrada.");
            return;
        } else {
            System.out.println("A porta COM foi encontrada.");
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            // open the streams
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port. This will prevent
     * port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            System.out.println("A porta COM foi fechada.");
        }
    }

    /**
     * This Method can be called to print a String to the serial connection
     */
    public synchronized void sendString(String msg) {
        try {
            msg += '\n';//add a newline character
            output.write(msg.getBytes());//write it to the serial
            output.flush();//refresh the serial
            System.out.print("<- " + msg);//output for debugging
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public void enviaDadosLed(int opcao) {
        try {
            byte[] enviar = new byte[5]; //NO FINAL É O TAMANHO

            enviar[0] = (byte) opcao;

            output.write(enviar, 0, 1);
            output.flush();
            System.out.println("output.write(" + enviar + ", 0, 1);");

        } catch (Exception e) {
            System.out.println("NAO FOI POSSIVEL ENVIAR OS DADOS PARA LIGAR O LED");
        }
    }

    /**
     * This Method is called when a command is recieved and needs to be encoded
     */
    private synchronized void encodeCommand(String com) {
        if (com.indexOf("s:") == 0) {//checks if the String starts with s for set
            String id = com.substring(com.indexOf("s:") + 2, com.indexOf(","));//remove the s, and store the "p1"
            String value = com.substring(com.indexOf(",") + 1, com.length());//store everything after the ","
            if (id.equals("p1") && !value.equals("")) {//if it's my poti1 and it sends a value
                String myValue = "s:s1," + value;//set the value to my servo1
                sendString(myValue);//and send it via the serial
            } else {
                System.out.println("not correct values");
            }
        }
    }

    int contaCS = 0;                //PARA CONTAR QUANTAS VEZES PASSA NO CHECK SUN, PARA FAZERMOS A VALIDAÇÃO
    //String RFID_FINAl_MSG = "";
    String dataRfid = "";           //PARA GUARDAR NUM SO STRING TODOS OS BYTES REFERENTES AO RFID
    String rfidFinalMostrar = "";

    @Override
    public synchronized void serialEvent(SerialPortEvent spe) {

        if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                if (input.available() > 0) {

                   //ig.EscreveEstadoDaMaquinaEstados("ESPERA_FF\n");
                    teste = new byte[16];

                    String valorFF = "0xff";
                    String valor01 = "0x01";
                    String valor06 = "0x06";
                    String valor10 = "0x10";

                    byte teste = (byte) (input.read() & 0xFF);
                    StringBuilder sb = new StringBuilder();
                    sb.append("0x" + String.format("%02x", teste));
                    //conta = conta + 1;
                    System.out.println("---> " + sb.toString());
                    String agora = sb.toString();
                    //System.out.println(" " + agora);
                    switch (estado) {

                        case "ESPERAFF":
                            if (agora.equals(valorFF)) //0xff
                            {
                                System.out.println("ESPERAFF    | VALOR_ARRAY : " + agora);
                                estado = "ESPERA01";        //VAMOS PARA O PROXIMO ESTADO
                                rfid_arrayList.add(agora);  //ADICIONAMOS AO ARRAY LIST
                                //ig.EscreveEstadoDaMaquinaEstados("ESPERA_01");
                            }
                            break;
                        case "ESPERA01":
                            if (agora.equals(valor01))//0x01
                            {
                                System.out.println("ESPERA01    | VALOR_ARRAY : " + agora);
                                estado = "ESPERA06";        //VAMOS PARA O PROXIMO ESTADO
                                rfid_arrayList.add(agora);  //ADICIONAMOS AO ARRAY LIST  
                                //ig.EscreveEstadoDaMaquinaEstados("ESPERA_06");
                            }

                            break;
                        case "ESPERA06":
                            if (agora.equals(valor06))//0x06
                            {
                                System.out.println("ESPERA06    | VALOR_ARRAY : " + agora);
                                estado = "ESPERA10";      //VAMOS PARA O PROXIMO ESTADO
                                rfid_arrayList.add(agora);  //ADICIONAMOS AO ARRAY LIST   
                                // ig.EscreveEstadoDaMaquinaEstados("ESPERA_10");
                            }
                            break;

                        case "ESPERA10":
                            if (agora.equals(valor10))//0x10
                            {
                                System.out.println("ESPERA10    | VALOR_ARRAY : " + agora);
                                estado = "ESPERADATA";      //VAMOS PARA O PROXIMO ESTADO
                                rfid_arrayList.add(agora);  //ADICIONAMOS AO ARRAY LIST
                                contador = 0;               //COLOCAR O CONTADOR DA DATA A ZERO
                                //ig.EscreveEstadoDaMaquinaEstados("ESPERA_data");
                            }
                            break;

                        case "ESPERADATA": //rfid alor

                            rfid_arrayList.add(agora);
                            contador++;     //INCREMENTAMOS O CONTADOR

                            System.out.println("contador : " + contador + " agora: " + agora);

                            if (contador == 5) {
                                estado = "ESPERACS";
                                contador = 0;   //LIMPAMOS O CONTADOR DA DATA   
                                contaCS = 0;    //LIMPAMOS O CONTADOR DO CHECK SUN
                                dataRfid = "";  //LIMPAMOS A STRING DA DATARFID
                                //ig.EscreveEstadoDaMaquinaEstados("ESPERA_CS");
                            }
                            break;

                        case "ESPERACS":
                            maquinaDeEstados = "ESPERA_FF";
                            rfid_arrayList.add(agora);
                            contaCS++;
                            System.out.println("ESTADO ESPERACS | CONTACS : " + contaCS + " | AGORA : " + agora);

                            if (contaCS == 3) {
                                for (int i = 4; i < 9; i++) {
                                    String verData = rfid_arrayList.get(i).toString();
                                    //System.out.println("VERDATA : " + verData);

                                    estado = "ESPERAFF";    //COLOCAR O ESTADO, NO ESTADO INICIAL

                                    //JUNTAR A DATA RFID TODA NUMA STRING
                                    dataRfid = dataRfid + verData;
                                    System.out.println("VERDATA : " + dataRfid + " |I -> " + i);

                                    if (i == 8) {
                                        if (rfid_arrayList.size() == 12) {

                                            ConverteDATAparaRFID();     //FUNCAO PARA CONVERTER PARA HEXADECIMAL O VALOR DO RFID

                                        } else {
                                            //MENSAGEM DE ERRO, A DZER QUE N RECEBEU OS BITES TODOS
                                            System.out.println("RFID INCOMPLETO, PASSE NOVAMENTE !!!");
                                        }

                                    }

                                }

                                //ESCREVER ARRAY DE DADOS
//                                for (int j = 0; j < rfid_arrayList.size(); j++){
//                                    System.out.println("BYTES : " + rfid_arrayList.get(j));
//                                }
                            }

                            break;
                        default:
                            estado = "ESPERAFF";    //ESTADO INICIAL
                            rfid_arrayList.clear(); //LIMPAMOS O ARRAY
                            break;
                    } //CLOSE SWITCH

                }

            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }

    public void ConverteDATAparaRFID() {
        System.out.println("FUNÇÃO ConverteDATAparaRFID");
        int tamanhoOriginal = dataRfid.length();

        int t1 = tamanhoOriginal / 5;   //VAI-NOS DAR OS 5 BYTES DA DATA DO RFID

        String D1 = dataRfid.substring(0, t1);
        int t2 = t1 + t1;

        String D2 = dataRfid.substring(t1, t2);
        int t3 = t2 + t1;

        String D3 = dataRfid.substring(t2, t3);
        int t4 = t3 + t1;

        String D4 = dataRfid.substring(t3, t4);

        String D5 = dataRfid.substring(t4, tamanhoOriginal);

        System.out.println("D1 " + D1);
        System.out.println("D2 " + D2);
        System.out.println("D3 " + D3);
        System.out.println("D4 " + D4);
        System.out.println("D5 " + D5);

        //VAMOS RETIRAR O 0X A STRING
        String D1F = "", D2F = "", D3F = "", D4F = "", D5F = "";

        D1F = D1.substring(2, 4);
        D2F = D2.substring(2, 4);
        D3F = D3.substring(2, 4);
        D4F = D4.substring(2, 4);
        D5F = D5.substring(2, 4);

        rfidFinalMostrar = D1F + " " + D2F + " " + D3F + " " + D4F + " " + D5F;
        System.out.println("FINAL : " + rfidFinalMostrar);

        //ENVIAR PARA A JANELA E LIMPAR TODAS AS VARIAVEIS ASSOCIADAS, PARA UMA PROXIMA LEITURA
        ig.accao(rfidFinalMostrar.toUpperCase());

        //LIGAR OS LED'S (CHAMAR A FUNÇÃO E ESCREVER)        
        int ligar = ig.LigaLed();

        //VER MAQUINA DE ESTADOS NA JANELA
        if (ligar == 1) {
            //VER O TEMPO
            enviaDadosLed(1);
            System.out.println("LIGAR 1");

        } else if (ligar == 2) {
            enviaDadosLed(2);
            System.out.println("LIGAR 2");
        }

        //LIMPAR CAMPOS 
        rfidFinalMostrar = "";      //LIMPAR VARIAVEL QUE MOSTRA RFID FINAL
        dataRfid = "";              //LIMPAR VARIVAEL DOS 5 BYTES DA DATA
        rfid_arrayList.clear();     //LIMPAR O ARRAY ONDE FICARAM GUARDADOS OS DADOS
        estado = "ESPERAFF";        //COLOCAR O ESTADO EM ESPERAFF
    }

//    public String enviaRFID() {
//        // System.out.println("ENVIA RFID : " + enviaRFID);
//        System.out.println("TAMNHO_STRING -> " + inputBuffer.length());
//
//        //FORMATAR STRING
//        int tamanhoOriginal = enviaRFID.length();
//        System.out.println("TAMANHO_ORIGINAL -> " + tamanhoOriginal);
//
//        //PASSAMOS O RFID PARA A INTERFACE  
//        ig.accao(enviaRFID);
//
//        //int led = ig.LigaLed();
//        enviaDadosLed(ig.LigaLed());
//        //System.out.println("PORTA_COM_LED -> " + ig.LigaLed());
//
//        //VERIFICAMOS O TAMANHO PARA LIMPAR O BUFFER
//        if (enviaRFID.length() == 15) {
//            inputBuffer = "";
//        }
//
//        return enviaRFID;
//
//    }
}
