var waiting = document.getElementById("waiting");
waiting.style.height = (window.screen.height - waiting.offsetTop -150) + "px";
if(!window.android){
    window.android = {
        "getAppMode" : () =>{
            return '1'
        },
        "getClipboard" : () =>{
            return "1,com.huawei.android.tips"
        },
        "setClipboard" : (data) =>{
            console.log("已经复制到剪贴板中: " + data);
        },
        "setApplist" : (data) =>{
            console.log("已经设置Applist: " + data);
        },
        "getApplist" : () =>{
            var temp = [{"appName":"com.android.cts.priv.ctsshim","packageName":"com.android.cts.priv.ctsshim","isSysApp":true,"isInWhiteList":false,"isInBlackList":false}, {"appName":"玩机技巧","packageName":"com.huawei.android.tips","isSysApp":true,"isInWhiteList":true,"isInBlackList":false}, {"appName":"全局复制","packageName":"com.camel.corp.universalcopy","isSysApp":false,"isInWhiteList":true,"isInBlackList":false}, {"appName":"Android Services Library","packageName":"com.google.android.ext.services","isSysApp":true,"isInWhiteList":false,"isInBlackList":false}, {"appName":"银联可信服务安全组件","packageName":"com.unionpay.tsmservice","isSysApp":false,"isInWhiteList":false,"isInBlackList":false}];
            return JSON.stringify(temp)
        },
    }
};
// 根据内容刷新view
var loadByContent = (mode, appList) =>{
    document.getElementById('mode_' + mode).checked = true;
    var package = document.getElementById('package_div');
    appList.sort((a,b) => {
        var a_in_list = (mode == '1' && a.isInWhiteList) || (mode == '2' && a.isInBlackList);
        var b_in_list = (mode == '1' && b.isInWhiteList) || (mode == '2' && b.isInBlackList);
        if(a_in_list == b_in_list)
            return 0;
        else if(a_in_list)
            return -1;
        else
            return 1;
        return a.age-b.age;
    });
    var newFrag = document.createDocumentFragment();
    var root = document.createElement("div");
    root.setAttribute("id", "package_div");
    appList.forEach((app) =>{
        var ul = document.createElement("ul");
        ul.className = "package";
        ul.setAttribute("pkg", app.packageName);
        var li1 = document.createElement("li");
        li1.className = "isSelect";
        var checkbox = document.createElement("input");
        checkbox.setAttribute("type", "checkbox");
        checkbox.setAttribute("pkg", app.packageName);
        if( (mode == '1' && app.isInWhiteList) || (mode == '2' && app.isInBlackList) )
            checkbox.setAttribute("checked", "checked");
        li1.append(checkbox);
        ul.append(li1);

        var li2 = document.createElement("li");
        li2.className = "appInfo";
        var div1 = document.createElement("div");
        var p1 = document.createElement("p");
        p1.className = "pkg-label";
        if(app.isSysApp)
            var txt1 = document.createTextNode("**" + app.appName);
        else
            var txt1 = document.createTextNode(app.appName);
        var div2 = document.createElement("div");
        var p2 = document.createElement("p");
        p2.className = "pkg-name";
        var txt2 = document.createTextNode(app.packageName);
        p1.append(txt1);
        div1.append(p1);
        li2.append(div1);
        p2.append(txt2);
        div2.append(p2);
        li2.append(div2);
        ul.append(li2);
        root.append(ul)
    });
    newFrag.append(root);
    package.replaceWith(newFrag);
    //document.getElementById('content').replaceChild(newFrag, package );
}
var setViewFromConfig = (data) =>{
    var list = data.split(",");
    var newFrag = document.createDocumentFragment();
    newFrag.appendChild(document.getElementById('package_div'));

    var ulChecked = new Array();
    var ulUnchecked = new Array();
    var root = newFrag.firstChild;
    newFrag.querySelectorAll("ul.package").forEach((node)=>{
        if(list.includes(node.attributes.pkg.value)){
            node.getElementsByTagName("input")[0].checked = true;
            ulChecked.push(node);
        }else{
            node.getElementsByTagName("input")[0].checked = false;
            ulUnchecked.push(node);
        }
    });
    ulChecked.forEach((node)=>{root.appendChild(node)});
    ulUnchecked.forEach((node)=>{root.appendChild(node)});
    document.getElementById('content').appendChild(newFrag);
    document.getElementById('mode_' + list[0]).checked = true;
}
var getConfigsFromView = () =>{
    var list = [];
    var mode = document.querySelector("input[name='mode']:checked").value;
    list.push(mode);
    document.querySelectorAll(".package").forEach((node)=>{
        if(node.querySelector("input").checked){
            list.push(node.querySelector(".pkg-name").innerText);
        }
    });
    return list;
}
var search = () =>{
    var searchTxt = txtSearch.value;
    console.log("searchTxt: " + searchTxt);
    var newFrag = document.createDocumentFragment();
    newFrag.appendChild(document.getElementById('package_div'));
    newFrag.querySelectorAll(".package").forEach((node, index)=>{
        if(node.innerText.indexOf(searchTxt) == -1){
            node.classList.add('hide');
        }else{
            node.classList.remove('hide');
        }
    });
    document.getElementById('content').appendChild(newFrag);
    return false;
}
var save = () =>{
    var list = getConfigsFromView();
    window.android.setApplist(list.toString());
}
var reload = () =>{
    console.log('------------------reload');
    // 获取模式包信息
    var mode = window.android.getAppMode();
    var appListStr = window.android.getApplist();
    var appList = JSON.parse(appListStr);
    loadByContent(mode, appList);
}
var import_settings = () =>{
    var data = window.android.getClipboard();
    if(data){
        setViewFromConfig(data);
    }
}
var export_settings = () =>{
    var list = getConfigsFromView();
    window.android.setClipboard(list.toString());
}
var btnReset = document.getElementById('settings-reset'); btnReset.onclick = reload;
var btnSave = document.getElementById('settings-save'); btnSave.onclick = save;
var btnImport = document.getElementById('settings-import'); btnImport.onclick = import_settings;
var btnExport = document.getElementById('settings-export'); btnExport.onclick = export_settings;
var btnSearch = document.getElementById('search');  btnSearch.onclick = search;
var txtSearch = document.getElementById('searchStr');
var radioMode1 = document.getElementById('mode_1');
var radioMode2 = document.getElementById('mode_2');
//reload();
setTimeout(reload, 1000);