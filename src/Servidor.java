import com.dinamonetworks.Dinamo;
import com.dinamonetworks.TacException;
import java.nio.charset.StandardCharsets;
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

        String keyIdServer = "key007";


        api.createKey("key007", TacNDJavaLib.ALG_RSA_4096);
        //System.out.println("ESTOU AQUI");

        // Gera a chave assimétrica no HSM
        //String keyIdServer = "kkeyK";
        /*byte[] publicKeyServer = api.genEcdhKeyX963(
            TacNDJavaLib.DN_GEN_KEY_X9_63_SHA256,                  // Operação de geração de chave
            keyIdServer,        // Identificador da chave privada do servidor
            null,               // Não é necessário chave pública do outro lado para geração
            TacNDJavaLib.ALG_AES_256, // Algoritmo da chave
            TacNDJavaLib.ALG_AES_256, // Atributos da chave
            null,               // Chave pública do cliente (ainda não disponível)
            null,               // Dados de derivação de chave (geralmente null ou empty)
            0                   // Flags de controle (geralmente 0)
            );*/
            
        byte[] publicKeyServer = api.genEcdhKey(TacNDJavaLib.ALG_RSA_4096_PUB, "key007", new byte[4096]);  // Passando um vetor vazio
        //System.out.println("Chave pública do servidor: " + new String(publicKeyServer));

        // Configura o servidor
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Servidor aguardando conexões na porta 5000...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Cliente conectado!");

        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        // Recebe a chave pública do cliente (Pa)
        int length = in.readInt();
        byte[] publicKeyCliente = new byte[length];
        in.readFully(publicKeyCliente);
        System.out.println("Chave pública do cliente recebida");

        // Gera o segredo compartilhado utilizando a chave pública do cliente
        byte[] secretShared = api.genEcdhKey(2, keyIdServer, publicKeyCliente);

        // Deriva a chave simétrica K a partir do segredo compartilhado
        byte[] keySymmetric = new byte[32];  // Vamos derivar uma chave simétrica de 256 bits
        System.arraycopy(secretShared, 0, keySymmetric, 0, 32);

        // Envia a chave pública do servidor ou C1 (no caso de KEM) para o cliente
        out.writeInt(publicKeyServer.length);
        out.write(publicKeyServer);
        out.flush();

        // Envia a mensagem cifrada (C2) para o cliente utilizando a chave simétrica K
        String mensagem = "Mensagem secreta do servidor!";
        byte[] mensagemBytes = mensagem.getBytes(StandardCharsets.UTF_8);
        byte[] mensagemCifrada = api.encrypt(keyIdServer, mensagemBytes);
        out.writeInt(mensagemCifrada.length);
        out.write(mensagemCifrada);
        out.flush();

        // Recebe a mensagem com a assinatura
        int lengthSig = in.readInt();
        byte[] signedMessage = new byte[lengthSig];
        in.readFully(signedMessage);

        // Verifica a assinatura da mensagem
        int verifyResult = api.verifySignature(keyIdServer, 1, signedMessage, mensagemBytes); // Assume SHA-256 (1)
        System.out.println("Verificação de assinatura: " + (verifyResult == 0 ? "Válida" : "Inválida"));

        // Fecha a conexão
        clientSocket.close();
        serverSocket.close();

        
    }
}
