(function($){
/*
    ͼƬ����Ч��
    add  2014-05-14 by  js�����
    ���͵�ַ��http://home.cnblogs.com/u/huzhiming/
    @jQuery or @String box : �����б�jQuery�������ѡ���� �磺����Ԫ��Ϊli�����ul
    @object config : {
            @Number width : һ�ι�����ȣ�Ĭ��Ϊbox�����һ��һ����Ԫ�ؿ��[�����Ԫ�ؿ�Ȳ����������Ч�������]
            @Number size : �б��ȣ�Ĭ��Ϊbox��������һ����Ԫ�ظ���[���size������һ����Ԫ�ظ�������֧��ѭ������]
            @Boolean loop : �Ƿ�֧��ѭ������ Ĭ�� true
            @Boolean auto : �Ƿ��Զ�����,֧���Զ�����ʱ����֧��ѭ������������������Ч,Ĭ��Ϊtrue
            @Number auto_wait_time : �Զ��ֲ�һ��ʱ����,Ĭ��Ϊ��3000ms
            @Function callback : ������ص�����������һ��������ǰ�����ڵ�����ֵ
        }
*/
function mggScrollImg(box,config){
    this.box = $(box);
    this.config = $.extend({},config||{});
    this.width = this.config.width||this.box.children().eq(0).width();//һ�ι����Ŀ��
    this.size = this.config.size||this.box.children().length;
    this.loop = this.config.loop||true;//Ĭ����ѭ������
    this.auto = this.config.auto||true;//Ĭ���Զ�����
    this.auto_wait_time = this.config.auto_wait_time||3000;//�ֲ����
    this.scroll_time = 300;//����ʱ��
    this.minleft = -this.width*(this.size-1);//��Сleftֵ��ע���Ǹ���[��ѭ������µ�ֵ]
    this.maxleft =0;//���lfetֵ[��ѭ������µ�ֵ]
    this.now_left = 0;//��ʼλ����Ϣ[��ѭ������µ�ֵ]
    this.point_x = null;//��¼һ��x����
    this.point_y = null;//��¼һ��y����
    this.move_left = false;//��¼���ı߻���
    this.index = 0;
    this.busy = false;
    this.timer;
    this.init();
}
$.extend(mggScrollImg.prototype,{
    init : function(){
        this.bind_event();
        this.init_loop();
        this.auto_scroll();
    },
    bind_event : function(){
        var self = this;
        self.box.bind('touchstart',function(e){
            if(e.touches.length==1 && !self.busy){
                self.point_x = e.touches[0].screenX;
                self.point_y = e.touches[0].screenY;
            }
        }).bind('touchmove',function(e){
            if(e.touches.length==1 && !self.busy){
                return self.move(e.touches[0].screenX,e.touches[0].screenY);//������ݷ���ֵ�����Ƿ���ֹĬ��touch�¼�
            }
        }).bind('touchend',function(e){
            !self.busy && self.move_end();
        });
    },
    /*
        ��ʼ��ѭ������,��һ������Ҫ���������Ԫ��ʱ���ݲ�֧��ѭ������Ч��,
        �����ʵ��һ���Թ��������Ԫ��Ч��������ͨ��ҳ��ṹʵ��
        ѭ������˼·��������β�ڵ㵽β��
    */
    init_loop : function(){
        if(this.box.children().length == this.size && this.loop){//��ʱֻ֧��size���ӽڵ�����������ѭ��
            this.now_left = -this.width;//���ó�ʼλ����Ϣ
            this.minleft = -this.width*this.size;//��Сleftֵ
            this.maxleft = -this.width;
            this.box.prepend(this.box.children().eq(this.size-1).clone()).append(this.box.children().eq(1).clone()).css(this.get_style(2));
            this.box.css('width',this.width*(this.size+2));
        }else{
            this.loop = false;
            this.box.css('width',this.width*this.size);
        }
    },
    auto_scroll : function(){//�Զ�����
        var self = this;
        if(!self.loop || !self.auto)return;
        clearTimeout(self.timer);
        self.timer = setTimeout(function(){
            self.go_index(self.index+1);
        },self.auto_wait_time);
    },
    go_index : function(ind){//������ָ������ҳ��
        var self = this;
        if(self.busy)return;
        clearTimeout(self.timer);
        self.busy = true;
        if(self.loop){//���ѭ��
            ind = ind<0?-1:ind;
            ind = ind>self.size?self.size:ind;
        }else{
            ind = ind<0?0:ind;
            ind = ind>=self.size?(self.size-1):ind;
        }
        if(!self.loop && (self.now_left == -(self.width*ind))){
            self.complete(ind);
        }else if(self.loop && (self.now_left == -self.width*(ind+1))){
            self.complete(ind);
        }else{
            if(ind == -1 || ind == self.size){//ѭ�������߽�
                self.index = ind==-1?(self.size-1):0;
                self.now_left = ind==-1?0:-self.width*(self.size+1);
            }else{
                self.index = ind;
                self.now_left = -(self.width*(self.index+(self.loop?1:0)));
            }
            self.box.css(this.get_style(1));
            setTimeout(function(){
                self.complete(ind);
            },self.scroll_time);
        }
    },
    complete : function(ind){//������ɻص�
        var self = this;
        self.busy = false;
        self.config.callback && self.config.callback(self.index);
        if(ind==-1){
            self.now_left = self.minleft;
        }else if(ind==self.size){
            self.now_left = self.maxleft;
        }
        self.box.css(this.get_style(2));
        self.auto_scroll();
    },
    next : function(){//��һҳ����
        if(!this.busy){
            this.go_index(this.index+1);
        }
    },
    prev : function(){//��һҳ����
        if(!this.busy){
            this.go_index(this.index-1);
        }
    },
    move : function(point_x,point_y){//������Ļ������
        var changeX = point_x - (this.point_x===null?point_x:this.point_x),
            changeY = point_y - (this.point_y===null?point_y:this.point_y),
            marginleft = this.now_left, return_value = false,
            sin =changeY/Math.sqrt(changeX*changeX+changeY*changeY);
        this.now_left = marginleft+changeX;
        this.move_left = changeX<0;
        if(sin>Math.sin(Math.PI/3) || sin<-Math.sin(Math.PI/3)){//������Ļ�Ƕȷ�Χ��PI/3  -- 2PI/3
            return_value = true;//����ֹĬ����Ϊ
        }
        this.point_x = point_x;
        this.point_y = point_y;
        this.box.css(this.get_style(2));
        return return_value;
    },
    move_end : function(){
        var changeX = this.now_left%this.width,ind;
        if(this.now_left<this.minleft){//��ָ���󻬶�
            ind = this.index +1;
        }else if(this.now_left>this.maxleft){//��ָ���һ���
            ind = this.index-1;
        }else if(changeX!=0){
            if(this.move_left){//��ָ���󻬶�
                ind = this.index+1;
            }else{//��ָ���һ���
                ind = this.index-1;
            }
        }else{
            ind = this.index;
        }
        this.point_x = this.point_y = null;
        this.go_index(ind);
    },
    /*
        ��ȡ������ʽ��Ҫ���ݸ����������������չ�÷���
        @int fig : 1 ���� 2  û����
    */
    get_style : function(fig){
        var x = this.now_left ,
            time = fig==1?this.scroll_time:0;
        return {
            '-webkit-transition':'-webkit-transform '+time+'ms',
            '-webkit-transform':'translate3d('+x+'px,0,0)',
            '-webkit-backface-visibility': 'hidden',
            'transition':'transform '+time+'ms',
            'transform':'translate3d('+x+'px,0,0)'
        };
    }
});
/*
    ��������ṩ���ýӿڣ������ṩ�ӿڷ���
    next ����һҳ
    prev ����һҳ
    go ��������ָ��ҳ
*/
$.mggScrollImg = function(box,config){
    var scrollImg = new mggScrollImg(box,config);
    return {//�����ṩ�ӿ�
        next : function(){scrollImg.next();},
        prev : function(){scrollImg.prev();},
        go : function(ind){scrollImg.go_index(parseInt(ind)||0);}
    }
}
})(Zepto)