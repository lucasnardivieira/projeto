import com.dinamonetworks.Dinamo;
import com.dinamonetworks.TacException;
import java.nio.charset.StandardCharsets;
import java.net.*;
import java.io.*;
import com.dinamonetworks.*;
import br.com.trueaccess.*;

public class Cliente {
    static String hsmIp = "187.33.9.132";
    static String hsmUser = "utfpr1";
    static String hsmUserPassword = "segcomp20241";

    public static void main(String[] args) throws TacException, IOException {
        // Inicia a conexão com o HSM
        Dinamo api = new Dinamo();
        api.openSession(hsmIp, hsmUser, hsmUserPassword);

        // Conecta-se ao servidor
        Socket socket = new Socket("localhost", 5000);
        System.out.println("Conectado ao servidor!");

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Mensagem a ser enviada
        String mensagemOriginal = "Mensagem secreta para o servidor!";
        byte[] mensagem = mensagemOriginal.getBytes(StandardCharsets.UTF_8);

        // Envia a mensagem para o servidor
        out.writeInt(mensagem.length);
        out.write(mensagem);
        out.flush();

        // Recebe a mensagem cifrada do servidor
        int length = in.readInt();
        byte[] mensagemCifrada = new byte[length];
        in.readFully(mensagemCifrada);

        // Descriptografa a mensagem recebida
        byte[] iv = new byte[16];  // IV simples com zeros
        String keyId = "key_aes_example"; // Chave AES usada no servidor
        byte[] mensagemDecifrada = api.decrypt(keyId, mensagemCifrada, iv, TacNDJavaLib.D_PKCS5_PADDING, TacNDJavaLib.MODE_CBC);

        // Exibe a mensagem descriptografada
        System.out.println("Mensagem recebida e descriptografada: " + new String(mensagemDecifrada, StandardCharsets.UTF_8));

        // Fecha a conexão
        socket.close();
    }
}
