import com.sun.crypto.provider.BlowfishCipher;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main {

    /**
     * 利用java原生的类实现SHA256加密
     * @param str 加密后的报文
     * @return
     */
    public static String getSHA256(String str){
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodestr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    /**
     * 将byte转为16进制
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes){
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i=0;i<bytes.length;i++){
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length()==1){
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    //根据map的value获得key
    public static int getKey(Map map,char value){
        Set set = map.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            if (entry.getValue().equals(value)){
                return (int)entry.getKey();
            }
        }
        return -1;
    }

    //设置block的allSha及256bit
    public static void setBlock(Block block){
        StringBuffer r = new StringBuffer("0000000000000000000000000000000000000000000000000000000000000000");
        String all = r.toString()+block.preSha+block.data+block.curSha;
        StringBuffer stringBuffer = new StringBuffer(getSHA256(all));
        Map<Integer,Character> map = new HashMap(){{
            put(0,'0');
            put(1,'1');
            put(2,'2');
            put(3,'3');
            put(4,'4');
            put(5,'5');
            put(6,'6');
            put(7,'7');
            put(8,'8');
            put(9,'9');
            put(10,'a');
            put(11,'b');
            put(12,'c');
            put(13,'d');
            put(14,'e');
            put(15,'f');
            put(16,'g');

        }};
        int flag = 63;
        while (!(stringBuffer.charAt(0)=='0'&&stringBuffer.charAt(1)=='0')) {
            if (r.charAt(flag)=='g') {
                r.replace(flag,flag+1,"0");
                flag--;
                r.replace(flag,flag+1,map.get(getKey(map,r.charAt(flag))+1)+"");
                //System.out.println(r);
            }else{
                flag=63;
                r.replace(63,64,map.get(getKey(map,r.charAt(flag))+1)+"");
                if (r.charAt(flag)!='g'){
                    stringBuffer = new StringBuffer(getSHA256(r.toString()+block.preSha+block.data+block.curSha));
                }
                //System.out.println(r);
            }
        }
        block.redundancy = r.toString();
        block.allSha = stringBuffer.toString();

        System.out.println("allSha: "+block.allSha);
        System.out.println("redundancy: "+block.redundancy);
        System.out.println("preSha: "+block.preSha);
        System.out.println("curSha: "+block.curSha);
        System.out.println("data: "+block.data);
    }

    //初始化创世块
    public static void  init(){
        Block genesisBlock = new Block();
        //genesisBlock.data=ReadFile.readTxtFile("/home/xzc/workplace/IdeaProjects/block/src/a.txt");
        genesisBlock.data = "data of genesisBlock";
        genesisBlock.curSha=getSHA256(genesisBlock.data);
        genesisBlock.preSha = "";
        setBlock(genesisBlock);
        writeBlock(genesisBlock);
    }

    //将block写入文件，用的绝对路径
    private static void writeBlock(Block genesisBlock) {
        try{
            //String data = " This content will append to the end of the file";

            File file =new File("/home/xzc/workplace/IdeaProjects/block/blocks/"+genesisBlock.allSha+".block");
            //File file = new File("gen.block");
            //if file doesnt exists, then create it
            if(!file.exists()){
                file.createNewFile();
                System.out.println("new block success!");
            }

            //true = append file
            FileWriter fileWritter = new FileWriter(file,true);
            fileWritter.write(genesisBlock.allSha);
            fileWritter.write(genesisBlock.redundancy);
            fileWritter.write(genesisBlock.preSha);
            fileWritter.write(genesisBlock.curSha);
            fileWritter.write(genesisBlock.data);
            fileWritter.close();

            System.out.println(" Write Done");

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //获得尾部block的allSha，即当前block的preSha
    public static String getPreSha(){

        List<String> files =  getFiles();

        String tmpBlock="";

        if (files.size()==1){
            //System.out.println(files.get(0));
            //System.out.println("11"+readBlock(files.get(0)));
            return readBlock(files.get(0)).substring(0,64);
        }else {
            String preSha="005ab5e7dd9fd9d7aadf283493e9222db0a541a558e5b241930789cc61bec465";
            //从列表中去除创世块
            for (int i=0;i<files.size();i++) {
                if (readBlock(files.get(i)).substring(0,64).equals(preSha)) {
                    files.remove(i);
                }
            }

            for (int i=0;i< files.size();i++){
                String curSha = readBlock(files.get(i)).substring(128,192);
                if (curSha.equals(preSha)) {
                    preSha = readBlock(files.get(i)).substring(0,64);
                    files.remove(i);
                    i=-1;
                }
            }
            return preSha;
        }
    }

    //获得文件目录，返回list
    private static List<String> getFiles() {
        List<String> files = new ArrayList<String>();
        String path = "/home/xzc/workplace/IdeaProjects/block/blocks";
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                files.add(tempList[i].toString());
                //文件名，不包含路径
                //String fileName = tempList[i].getName();
                //System.out.println(tempList[i].toString());
            }
        }
        return files;
    }

    //读取block返回String
    public static String readBlock(String filePath){
        String res = "";
        try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    //System.out.println(lineTxt);
                    res+=lineTxt;
                }
                read.close();
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return res;
    }

    //新建block
    public static Block newBlock(String data){
        Block block = new Block();
        block.data = data;
        block.preSha = getPreSha();
        //System.out.println("preSha: "+block.preSha);
        block.curSha = getSHA256(block.data);
        setBlock(block);
        writeBlock(block);
        return block;
    }

    public static void main(String[] args) {

        init();//生成创世块
        String data="data of new block1";
        Block block1= newBlock(data);
        data="data of new block2";
        Block block2= newBlock(data);
        data="data of new block3";
        Block block3= newBlock(data);
    }
}
