import com.dinamonetworks.Dinamo;
import com.dinamonetworks.TacException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.net.*;
import java.io.*;
import com.dinamonetworks.*;
import br.com.trueaccess.*;

public class Servidor {
    static String hsmIp = "187.33.9.132";
    static String hsmUser = "utfpr1";
    static String hsmUserPassword = "segcomp20241";

    public static void main(String[] args) throws TacException, IOException {
        // Inicia a conexão com o HSM
        Dinamo api = new Dinamo();
        api.openSession(hsmIp, hsmUser, hsmUserPassword);

        // Cria uma chave AES no HSM
        String keyId = "key_aes_example";
        api.createKey(keyId, TacNDJavaLib.ALG_AES_256);

        // Configura o servidor
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Servidor aguardando conexões na porta 5000...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Cliente conectado!");

        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        // Recebe a mensagem do cliente
        int length = in.readInt();
        byte[] mensagemRecebida = new byte[length];
        in.readFully(mensagemRecebida);

        // Criptografa a mensagem com a chave AES
        byte[] iv = new byte[16];  // IV simples com zeros
        byte[] mensagemCifrada = api.encrypt(keyId, mensagemRecebida, iv, TacNDJavaLib.D_PKCS5_PADDING, TacNDJavaLib.MODE_CBC);

        // Envia a mensagem cifrada de volta ao cliente
        out.writeInt(mensagemCifrada.length);
        out.write(mensagemCifrada);
        out.flush();

        // Fecha a conexão
        clientSocket.close();
        serverSocket.close();

        // Exclui a chave do HSM
        // api.deleteKeyIfExists(keyId);
        System.out.println("Chave excluída do HSM.");
    }
}
