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

        // Gera a chave assimétrica no HSM
        String keyIdClient = "key_client";
        //byte[] publicKeyClient = api.genEcdhKey(1, keyIdClient, new byte[0]); // Gerando chave pública do cliente
        //System.out.println("Chave pública do cliente: " + new String(publicKeyClient));

        // Conecta-se ao servidor
        Socket socket = new Socket("localhost", 5000);
        System.out.println("Conectado ao servidor!");

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Envia a chave pública do cliente (Pa) para o servidor
        //out.writeInt(publicKeyClient.length);
        //out.write(publicKeyClient);
        out.flush();

        // Recebe a chave pública do servidor ou C1 (no caso de KEM)
        int length = in.readInt();
        byte[] publicKeyServer = new byte[length];
        in.readFully(publicKeyServer);
        System.out.println("Chave pública do servidor recebida");

        // Gera o segredo compartilhado utilizando a chave pública do servidor
        byte[] secretShared = api.genEcdhKey(2, keyIdClient, publicKeyServer);

        // Deriva a chave simétrica K a partir do segredo compartilhado
        byte[] keySymmetric = new byte[32];  // Vamos derivar uma chave simétrica de 256 bits
        System.arraycopy(secretShared, 0, keySymmetric, 0, 32);

        // Recebe a mensagem cifrada (C2) do servidor
        int lengthC2 = in.readInt();
        byte[] mensagemCifrada = new byte[lengthC2];
        in.readFully(mensagemCifrada);

        // Descriptografa a mensagem utilizando a chave simétrica K
        byte[] mensagemDecifrada = api.decrypt(keyIdClient, mensagemCifrada);

        // Exibe a mensagem descriptografada
        System.out.println("Mensagem recebida e descriptografada: " + new String(mensagemDecifrada, StandardCharsets.UTF_8));

        // Gera um par de chaves para assinatura digital
        byte[] signature = api.signHash(keyIdClient, 1, mensagemDecifrada);  // Assume SHA-256 (1)

        // Envia a mensagem e a assinatura para o servidor
        out.writeInt(mensagemDecifrada.length);
        out.write(mensagemDecifrada);
        out.writeInt(signature.length);
        out.write(signature);
        out.flush();

        // Fecha a conexão
        socket.close();
    }
}
