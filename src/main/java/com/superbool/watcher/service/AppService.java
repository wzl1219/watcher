package com.superbool.watcher.service;

import com.superbool.watcher.mapper.AppMapper;
import com.superbool.watcher.model.AppModel;
import com.superbool.watcher.model.ResponseModel;
import com.superbool.watcher.service.external.GrafanaService;
import com.superbool.watcher.util.Monitor;
import com.superbool.watcher.util.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Created by kofee on 16/7/24.
 */
@Service
public class AppService {
    private static final Logger logger = LoggerFactory.getLogger(AppService.class);

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private GrafanaService grafanaService;

    //注册一个应用
    public ResponseModel register(AppModel appModel) {
        long begin = System.currentTimeMillis();
        ResponseModel response = new ResponseModel(-1, "服务器错误", null);
        try {
            //生成一个随机的token
            String token = UUID.randomUUID().toString();
            appModel.setToken(token);

            //先在app表中将应用的信息插入
            int result = appMapper.addApp(appModel);

            if (result == 1) {
                response = refreshDashboard(appModel);

            } else {
                response.setMsg("插入数据库失败");
            }

            Monitor.recordOne("注册应用", System.currentTimeMillis() - begin);

        } catch (DuplicateKeyException e) {
            Monitor.recordOne("注册应用重复", System.currentTimeMillis() - begin);
            logger.warn("注册应用重复 app={}", appModel, e);

        } catch (Exception e) {
            Monitor.recordOne("注册应用失败", System.currentTimeMillis() - begin);
            logger.error("注册应用失败 app={}", appModel, e);
        }

        return response;
    }

    //有可能插入数据库成功,创建dashboard失败,所以单独抽出来
    public ResponseModel refreshDashboard(AppModel appModel) {
        ResponseModel response = new ResponseModel(-1, "服务器错误", null);
        //数据库插入成功,创建dashboard
        String result = grafanaService.crateDashboard(appModel);

        JsonObject resultJson = new Gson().fromJson(result, JsonObject.class);

        if (resultJson.get("status").equals("success")) {
            PullDataService.addApp(appModel);
            response.setCode(0);
            response.setMsg(appModel.getToken());
        } else {
            response.setMsg("插入数据库成功,但创建grafana失败");
        }
        return response;
    }


    //更新应用的信息
    public ResponseModel update(AppModel appModel) {
        ResponseModel response = new ResponseModel(-1, "服务器错误", null);
        return response;
    }

    //查询某个应用信息
    public AppModel getByGroupAndName(String group, String name) {
        ResponseModel response = new ResponseModel(-1, "服务器错误", null);
        return null;
    }

    //查询一个组的所有应用
    public List<AppModel> getByGroup(String group) {
        return null;
    }

    //查询一个分组下的所有应用
    public List<AppModel> getByOrganization(int organization) {
        return null;
    }

    //查询某个联系人的所有应用
    public List<AppModel> getByContact(String contact) {
        return null;
    }

    //查询所有的应用
    public List<AppModel> getAllPullApp() {
        long begin = System.currentTimeMillis();
        List<AppModel> appModelList = appMapper.getByStyle(Constants.PULL);
        Monitor.recordOne("get_all_pull_app", System.currentTimeMillis() - begin);
        return appModelList;
    }

    //删除一个应用  根据token 或者 group 和 name
    public ResponseModel delete(JsonObject appInfo) {
        ResponseModel response = new ResponseModel(-1, "服务器错误", null);
        return response;
    }


}
