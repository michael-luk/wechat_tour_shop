function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}

function GetListFromStrInSplit(str) {
    if (!str) return [""]
    if (str == "") return [""]

    var strForSplit = str
    if (str.lastIndexOf(',') == str.length - 1) {
        strForSplit = str.substring(0, str.lastIndexOf(','))
    }
    var list = strForSplit.split(",")
    if (list.length <= 0) {
        return [""]
    }
    return list
}

function changeCaptcha (tag) {
    var newSrc = "/captcha?tag=" + tag + "&t=" + new Date().valueOf();
    $("#captchaField").attr("value", "");
    $("#captcha").attr("src", newSrc);
}

function showAlert(title, msg, type) {
    $("body").BSAlert( title, msg, {
            type : type,
            display : {
                position : 'absolute',
                bottom : '20px',
                right : '20px'
            },
            autoClose : true,
            duration : 1500
        }
    );
}

var swapItems = function(arr, index1, index2) {
    arr[index1] = arr.splice(index2, 1, arr[index1])[0];
    return arr;
};