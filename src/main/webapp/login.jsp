<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<head>
    <title>С���۵�¼</title>
    <link rel="stylesheet" media="screen" href="css/style.css">
    <link rel="stylesheet" type="text/css" href="css/reset.css"/>
</head>
<body>

<div id="particles-js">
    <div class="login">
        <form id="userLoginForm">
        <div class="login-top">
            ��¼
        </div>
        <div class="login-center clearfix">
            <div class="login-center-img"><img src="img/name.png"/></div>
            <div class="login-center-input">
                <input type="text" name="userName" value="" placeholder="�����������û���" onfocus="this.placeholder=''" onblur="this.placeholder='�����������û���'"/>
                <div class="login-center-input-text">�û���</div>
            </div>
        </div>
        <div class="login-center clearfix">
            <div class="login-center-img"><img src="img/password.png"/></div>
            <div class="login-center-input">
                <input type="password" name="password" hejivalue="" placeholder="��������������" onfocus="this.placeholder=''" onblur="this.placeholder='��������������'"/>
                <div class="login-center-input-text">����</div>
            </div>
        </div>
        <div class="login-button">
            ��½
        </div>
        </form>
    </div>
    <div class="sk-rotating-plane"></div>
</div>

<!-- scripts -->
<script src="js/particles.min.js"></script>
<script src="js/app.js"></script>
<script type="text/javascript" src="js/jquery/1.9.1/jquery.min.js"></script>
<script type="text/javascript">
    function hasClass(elem, cls) {
        cls = cls || '';
        if (cls.replace(/\s/g, '').length == 0) return false; //��clsû�в���ʱ������false
        return new RegExp(' ' + cls + ' ').test(' ' + elem.className + ' ');
    }

    function addClass(ele, cls) {
        if (!hasClass(ele, cls)) {
            ele.className = ele.className == '' ? cls : ele.className + ' ' + cls;
        }
    }

    function removeClass(ele, cls) {
        if (hasClass(ele, cls)) {
            var newClass = ' ' + ele.className.replace(/[\t\r\n]/g, '') + ' ';
            while (newClass.indexOf(' ' + cls + ' ') >= 0) {
                newClass = newClass.replace(' ' + cls + ' ', ' ');
            }
            ele.className = newClass.replace(/^\s+|\s+$/g, '');
        }
    }
    document.querySelector(".login-button").onclick = function(){
        addClass(document.querySelector(".login"), "active")
        setTimeout(function(){
            addClass(document.querySelector(".sk-rotating-plane"), "active")
            document.querySelector(".login").style.display = "none"
        },800)
        setTimeout(function(){
            removeClass(document.querySelector(".login"), "active")
            removeClass(document.querySelector(".sk-rotating-plane"), "active")
            document.querySelector(".login").style.display = "block"
            fun1();
        },0)
    }
    function fun1() {
        $.ajax({
            type:"post",
            url:"/loginUser",
            data:$("#userLoginForm").serialize(),
            //async:false,
            dataType:"json",
            success:function(data){
                            location.href='/index.jsp';
                    alert(data.msg)
            },
            error:function(data){
                alert(data.msg)
                console.log("����");
            },
        })
    }
</script>
</body>
</html>