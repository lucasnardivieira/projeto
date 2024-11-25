import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import com.dinamonetworks.*;
import br.com.trueaccess.*;

public class EncDec {
    static String hsmIp = "187.33.9.132";
    static String hsmUser = "utfpr1";
    static String hsmUserPassword = "segcomp20241";
    public static void main(String[] args) throws TacException {
    Dinamo api = new Dinamo();
    api.openSession(hsmIp, hsmUser, hsmUserPassword);
    byte[] iv = new byte[]{ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    String plainText = "asdfasdfasdfasdfasdfas";
    byte[] originalData = plainText.getBytes(StandardCharsets.UTF_8);
    String keyId = "SUA_KEY_ID_AQUI";
    api.createKey(keyId, TacNDJavaLib.ALG_AES_256);
    byte[] encrypted = api.encrypt( keyId, originalData, iv, TacNDJavaLib.D_PKCS5_PADDING, TacNDJavaLib.MODE_CBC );
    byte[] decrypted = api.decrypt( keyId, encrypted, iv, TacNDJavaLib.D_PKCS5_PADDING,TacNDJavaLib.MODE_CBC);
    if(!Arrays.equals(originalData, decrypted))
    {
    System.out.println("Dados decriptados não são iguais aos dados originais!");
    return;
    }
    System.out.println("Dados decriptados com sucesso.");
    api.deleteKeyIfExists(keyId);
    }
   }
   