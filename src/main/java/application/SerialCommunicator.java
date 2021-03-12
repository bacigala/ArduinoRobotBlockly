package application;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class SerialCommunicator {

    private SerialPort connectedPort = null;
    private OutputStream outputStream = null;

    public static class ComPort {
        private final String comName, fullName;

        public ComPort(String comName, String fullName) {
            this.comName = comName;
            this.fullName = fullName;
        }

        @Override
        public String toString() { return fullName; }

        public String getComName() { return comName; }

        public String getFullName() { return fullName; }
    }

    public ArrayList<ComPort> getAvailablePorts() {
        ArrayList<ComPort> result = new ArrayList<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            result.add(new ComPort(port.getSystemPortName(), port.getDescriptivePortName()));
        }
        return result;
    }

    public boolean connectToPort(ComPort comPort) {
        // check whether the port is still available and valid
        boolean isValid = false;
        for (SerialPort availablePort : SerialPort.getCommPorts()) {
            if (availablePort.getSystemPortName().equals(comPort.comName)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            System.err.println("Port " + comPort.fullName + "is no longer available.");
            return false;
        }

        try {
            connectedPort = SerialPort.getCommPort(comPort.comName);
            connectedPort.openPort();
            connectedPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            setOutputStream(outputStream);
        } catch (Exception e) {
            System.err.println("Unable to connect on port " + comPort.fullName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void setOutputStream(OutputStream outputStream) {
        if (outputStream == null) return;
        if (connectedPort == null) {
            this.outputStream = outputStream;
            return;
        }
        connectedPort.addDataListener( new SerialPortMessageListener() {
                @Override
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }

                @Override
                public byte[] getMessageDelimiter() { return new byte[] { (byte)10 }; }

                @Override
                public boolean delimiterIndicatesEndOfMessage() { return true; }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    try {
                        outputStream.write(event.getReceivedData());
                    } catch (IOException e) {
                        System.err.println("application.SerialCommunicator: Unable to write to OutputStream.");
                    }
                }
        });
    }

    public void send(String data) throws IOException {
        if (data == null || data.isEmpty()) return;
        if (connectedPort == null) {
            System.err.println("Unable to send data - no port connected.");
            return;
        }
        OutputStream out = connectedPort.getOutputStream();
        try {
            if (data.length() == 1) {
                out.write(data.charAt(0));
                return;
            }
            byte[] bytesToSend = new byte[data.length() + 2];
            for (int index = 0; index < data.length(); index++) {
                bytesToSend[index] = (byte)data.charAt(index);
            }
            bytesToSend[data.length()] = (byte)13;
            bytesToSend[data.length()+1] = (byte)10;
            out.write(bytesToSend);
            out.close();
        } catch (IOException e) {
            System.err.println("application.SerialCommunicator.send: Unable to write to COM port.");
            throw e;
        }
    }

    public void disconnect() {
        if (connectedPort != null) connectedPort.closePort();
        connectedPort = null;
    }

}
