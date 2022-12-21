package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.util.Base64;

import static java.lang.System.out;
import static java.util.Arrays.copyOfRange;

public class Controller {

    @FXML
    private TextField textFile1;

    @FXML
    private TextField textDigitalSig;

    @FXML
    private TextField textFile2;

    @FXML
    private Button btnSearchFile1;

    @FXML
    private Button btnSearchFile2;

    @FXML
    private Button btnDigitalSig;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnVerification;

    public String path;
    public String path1;

    byte[] bufferReadFile1;
    byte[] bufferReadFile2;
    byte[] buffer3;
    byte[] data;

    KeyPairGenerator generator;
    KeyPair keyPair;
    String fileSignatureString;
    Signature dsa;

    @FXML
    void initialize() {

        btnSearchFile1.setOnAction(actionEvent -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("My File Chooser");
            File f = fc.showOpenDialog(null);

            if (f != null)
            {
                textFile1.setText(f.getAbsolutePath());
                path = f.getAbsolutePath();
            }
        });

        btnSearchFile2.setOnAction(actionEvent -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("My File Chooser");
            File f = fc.showOpenDialog(null);

            if (f != null)
            {
                textFile2.setText(f.getAbsolutePath());
                path1 = f.getAbsolutePath();
            }

        });

        btnDigitalSig.setOnAction(actionEvent -> {
            ReadFile1(path);
            try {
                KeyPairPriv();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            }

            fileSignatureString = Base64.getEncoder().encodeToString(buffer3);




            textDigitalSig.setText(fileSignatureString);
        });

        btnSave.setOnAction(actionEvent -> {

            OutputStream os = null;
            try {
                os = new FileOutputStream("Signature.txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                os.write(buffer3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnVerification.setOnAction(actionEvent -> {
            ReadFile2(path1);
            try {
                KeyPairPublic();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }

        });
    }

    public void ReadFile1(String path){
        try {
            try (FileInputStream fin = new FileInputStream(path)) {
                String p;
                System.out.println(path);
                bufferReadFile1 = new byte[fin.available()];
                fin.read(bufferReadFile1, 0, bufferReadFile1.length);
                fin.close();
            }


        } catch (IOException e)
        {
            e.printStackTrace();
            out.println("Error: " + e);
        }
    }
    public void ReadFile2(String path1){
        try {
            try (FileInputStream fin = new FileInputStream(path1)) {
                String p;
                System.out.println(path1);
                bufferReadFile2 = new byte[fin.available()];
                fin.read(bufferReadFile2, 0, bufferReadFile2.length);
                fin.close();
            }


        } catch (IOException e)
        {
            e.printStackTrace();
            out.println("Error: " + e);
        }
    }
    private void showAlert(Alert.AlertType type, String title, String content){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void KeyPairPriv() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException {
        generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        keyPair = generator.generateKeyPair();
        dsa = Signature.getInstance("SHA256withRSA");
        dsa.initSign(keyPair.getPrivate());
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        for (int i = 0; i < bufferReadFile1.length/117 + 1; i++) {
            byte[] buffer1;
            if (i < bufferReadFile1.length / 117) {
                buffer1 = copyOfRange(bufferReadFile1, i * 117, (i + 1) * 117);
            } else {
                buffer1 = copyOfRange(bufferReadFile1, i * 117, bufferReadFile1.length);
            }
            data = cipher.doFinal(buffer1);
            dsa.update(data);
            buffer3 = dsa.sign();
        }
    }

    public void KeyPairPublic() throws InvalidKeyException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException {
        dsa.initVerify(keyPair.getPublic());
        dsa.update(data);
        boolean verifies = dsa.verify(bufferReadFile2);
        System.out.println("Signature is ok: " + verifies);
        if(verifies) {
            showAlert(Alert.AlertType.INFORMATION, "Signature Verification", "Verified");
        } else {
            showAlert(Alert.AlertType.WARNING, "Signature Verification", "Not verified");
        }
    }

}

