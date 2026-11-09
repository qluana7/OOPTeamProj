import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Interactive Swing presentation of the thirteen ref.pdf screens. Demo data only. */
public class CampusQuiz {
    enum Screen { WELCOME, GUIDE, PARTS, INTRO, QUESTION, SELECTED, CORRECT, INCORRECT, EXIT, PART_DONE, FINAL, REVIEW, REVIEW_DETAIL }
    static final int WIDTH=1000, HEIGHT=1080;
    static final Color INK=new Color(16,17,18), MUTED=new Color(101,106,115), LINE=new Color(211,212,217), PALE=new Color(243,244,246), BLUE=new Color(236,243,253), RED=new Color(190,64,55);
    static final String[] PARTS={"KGU CAMPUS GUIDE","ACADEMIC LIFE","BUILDING QUIZ"};
    static final String[] TAGLINES={"경기대, 얼마나 알고 있나요?","학사·학습생활 정복하기!","사진으로 맞히는 경기대 건물!"};
    static final String[] OPTIONS={"캠퍼스 안내(VR 캠퍼스)","학생 개인별 출석 점수","교수 개인별 연구실적","실시간 강의실 공석 현황"};
    static final String EXPLANATION="경기대학교 입학처의 KGU소개 메뉴에서는 캠퍼스 안내\n(VR 캠퍼스)를 통해 온라인으로 학교 캠퍼스를 둘러볼 수\n있습니다.";
    record QuizQuestion(String prompt,String[] options,int answer,String explanation,String image){QuizQuestion(String prompt,String[] options,int answer,String explanation){this(prompt,options,answer,explanation,null);}}
    record MissedQuestion(int part,int number,QuizQuestion question,int selected){}
    static final QuizQuestion[] CAMPUS_QUIZ={
        new QuizQuestion("수업이 끝난 뒤 제1복지관에서 현금을 인출하고, 이어서 우편물을 보내려 한다.\n두 업무를 한 건물 안에서 해결하려면 어디로 가는 것이 가장 적절할까?",
            new String[]{"제1복지관 1층","제1복지관 지하 1층","학생회관 5층","제2강의동(성신관) 1층"},0,"제1복지관 1층에서 현금 인출과 우편 업무를 함께 해결할 수 있습니다."),
        new QuizQuestion("공강 시간이 짧아 교내에서 식사를 해결하려 한다. 경기대 수원캠퍼스에서\n‘경슐랭’을 이용하려면 어느 위치를 찾아가야 할까?",
            new String[]{"학생회관 5층","제1복지관 지하 1층","제1복지관 3층","제2강의동(성신관) 1층"},1,"경슐랭은 제1복지관 지하 1층에 있습니다."),
        new QuizQuestion("팀 프로젝트를 마친 뒤 잠시 쉬거나 학생들이 함께 활용하는 공간을 찾고 있다.\n학생회관의 K-Space는 몇 층에 소개되어 있을까?",
            new String[]{"학생회관 2층","학생회관 3층","학생회관 4층","학생회관 5층"},3,"학생회관 K-Space는 5층에 있습니다."),
        new QuizQuestion("수업 전후로 운동할 공간을 찾고 있다. 학생언론의 캠퍼스 소개에 따르면\n헬스장과 1층 카페 ING가 함께 언급된 건물은 어디일까?",
            new String[]{"제1강의동(진리관)","제2강의동(성신관)","제7강의동(집현관)","제9강의동(호연관)"},1,"헬스장과 1층 카페 ING가 함께 소개된 건물은 제2강의동(성신관)입니다."),
        new QuizQuestion("동아리 또는 팀 과제 구성원들과 함께 사용할 회의 공간을 찾고 있다.\nSmart Meetingroom은 어디에 위치한 것으로 소개되었을까?",
            new String[]{"제1복지관 3층","제1복지관 지하 1층","학생회관 5층","이스퀘어 2층"},0,"Smart Meetingroom은 제1복지관 3층에 있습니다.")
    };
    static final QuizQuestion[] ACADEMIC_QUIZ={
        new QuizQuestion("컴퓨터공학전공을 단일전공으로 이수할 경우\n졸업에 필요한 최소 학점은?",new String[]{"120학점","130학점","136학점","140학점"},2,"단일전공, 복수전공, 부전공 모두 졸업에 필요한 총 이수학점은 136학점이다."),
        new QuizQuestion("컴퓨터공학전공을 단일전공으로 이수할 경우\n최소 전공 이수학점은?",new String[]{"42학점","60학점","67학점","75학점"},2,"컴퓨터공학전공 단일전공자의 최소 전공 이수학점은 67학점이다."),
        new QuizQuestion("다음 중 인성교양 필수 교과목은?",new String[]{"대학영어","진성애인성","이산수학","수리논리"},1,"진성애인성은 1학년에 이수하는 3학점 필수 인성교양 교과목이다."),
        new QuizQuestion("핵심교양을 이수하기 위한 올바른 조건은?",new String[]{"1개 영역에서 6학점","2개 이상 영역에서 6학점","3개 영역에서 9학점","모든 영역에서 1학점씩"},1,"핵심교양은 총 6개 영역 중 2개 이상의 영역에서 6학점을 이수해야 한다."),
        new QuizQuestion("다음 중 1학년 1학기에 개설되는 전공 교과목은?",new String[]{"자료구조론","C프로그래밍기초","운영체제","데이터베이스"},1,"C프로그래밍기초는 1학년 1학기에 개설되는 3학점 전공 교과목이다.")
    };
    static final QuizQuestion[] BUILDING_QUIZ={
        new QuizQuestion("사진 속 건물의 이름은 무엇일까요?",new String[]{"3강의동","1강의동","5강의동","학생회관"},1,"사진 속 건물은 1강의동입니다.","building-first-lecture"),
        new QuizQuestion("사진 속 건물의 이름은 무엇일까요?",new String[]{"어울림관","도서관","컨벤션센터","7강의동"},2,"사진 속 건물은 컨벤션센터입니다.","building-convention"),
        new QuizQuestion("사진 속 건물의 이름은 무엇일까요?",new String[]{"기숙사","9강의동","학생회관","도서관"},3,"사진 속 건물은 도서관입니다.","building-library")
    };
    static Font regular,bold,black;
    final DesignCanvas root=new DesignCanvas();
    final Map<String,BufferedImage> images=new HashMap<>();
    Screen screen=Screen.WELCOME, beforeExit=Screen.SELECTED;
    int part=0, selected=-1, reviewIndex=0, questionIndex=0, partScore=0;
    int[] partScores={0,0,0};
    final java.util.List<MissedQuestion> missed=new ArrayList<>();
    int reviewPage=0;
    JFrame frame;
    record Hotspot(String name,Rectangle2D.Double bounds,Runnable action,boolean enabled){}
    final java.util.List<Hotspot> hotspots=new ArrayList<>();

    static InputStream resource(String path)throws IOException{InputStream in=CampusQuiz.class.getResourceAsStream("/"+path);return in!=null?in:new FileInputStream(path);}
    static Font loadFont(String name,String fallback,int style){try(InputStream in=resource("assets/fonts/"+name)){return Font.createFont(Font.TRUETYPE_FONT,in);}catch(Exception e){return new Font(fallback,style,20);}}
    static void loadFonts(){regular=loadFont("NotoSansKR-Regular.otf","Noto Sans CJK KR",Font.PLAIN);bold=loadFont("NotoSansKR-Bold.otf","Noto Sans CJK KR",Font.BOLD);black=loadFont("NotoSansKR-Black.otf","Noto Sans CJK KR Black",Font.BOLD);}
    CampusQuiz(){for(String n:new String[]{"welcome-campus","part-campus","intro-campus","question-campus","feedback-campus","result-campus","academic","program","good-start","start-script","nice-work","trophy","clock","star","celebration","building-first-lecture","building-library","building-convention"})try(InputStream in=resource("assets/images/"+n+".png")){BufferedImage im=ImageIO.read(in);if(im==null)throw new IOException(n);images.put(n,im);}catch(IOException e){throw new IllegalStateException("Missing design asset: "+n,e);}show(Screen.WELCOME);}
    void show(Screen next){screen=next;rebuild();}
    void choose(int index){selected=index;show(Screen.SELECTED);}
    void startPart(int index){part=index;selected=-1;questionIndex=0;partScore=0;partScores[index]=0;missed.removeIf(m->m.part()==index);show(Screen.INTRO);}
    QuizQuestion currentQuestion(){return part==0?CAMPUS_QUIZ[questionIndex]:part==1?ACADEMIC_QUIZ[questionIndex]:BUILDING_QUIZ[questionIndex];}
    int questionCount(){return part==0?CAMPUS_QUIZ.length:part==1?ACADEMIC_QUIZ.length:BUILDING_QUIZ.length;}
    int totalQuestionCount(){return CAMPUS_QUIZ.length+ACADEMIC_QUIZ.length+BUILDING_QUIZ.length;}
    void nextAfterFeedback(){if(screen==Screen.CORRECT)partScore+=10;else missed.add(new MissedQuestion(part,questionIndex+1,currentQuestion(),selected));selected=-1;questionIndex++;if(questionIndex<questionCount())show(Screen.QUESTION);else{partScores[part]=partScore;show(Screen.PART_DONE);}}
    void reset(){part=0;selected=-1;reviewIndex=0;reviewPage=0;questionIndex=0;partScore=0;Arrays.fill(partScores,0);missed.clear();show(Screen.WELCOME);}
    void nextPart(){if(part<2)startPart(part+1);else show(Screen.FINAL);}
    void exitQuiz(){beforeExit=screen;show(Screen.EXIT);}
    void hit(String name,double x,double y,double w,double h,boolean enabled,Runnable action){hotspots.add(new Hotspot(name,new Rectangle2D.Double(x,y,w,h),action,enabled));}
    void rebuild(){
        root.removeAll();hotspots.clear();
        // Hit geometry and visible geometry share the same drawing path.
        BufferedImage scratch=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);Graphics2D g=scratch.createGraphics();draw(g,true);g.dispose();
        for(Hotspot spot:hotspots){JButton b=new JButton(spot.name()){
            protected void paintComponent(Graphics graphics){if(isFocusOwner()){Graphics2D g2=(Graphics2D)graphics.create();g2.setColor(new Color(40,97,210));g2.setStroke(new BasicStroke(2));g2.drawRoundRect(3,3,getWidth()-7,getHeight()-7,8,8);g2.dispose();}}
        };b.setEnabled(spot.enabled());b.setContentAreaFilled(false);b.setBorder(null);b.setOpaque(false);b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.getAccessibleContext().setAccessibleName(spot.name());b.addActionListener(e->spot.action().run());b.putClientProperty("bounds",spot.bounds());root.add(b);}
        root.revalidate();root.repaint();
    }
    class DesignCanvas extends JPanel {
        double scale=1,ox,oy;
        DesignCanvas(){setLayout(null);setBackground(new Color(229,230,233));setPreferredSize(new Dimension(800,864));getAccessibleContext().setAccessibleName("KGU QUIZ 화면");
            Point[] drag={null};addMouseListener(new MouseAdapter(){public void mousePressed(MouseEvent e){if(frame!=null&&(e.getY()-oy)/scale<40)drag[0]=e.getPoint();}public void mouseReleased(MouseEvent e){drag[0]=null;}});addMouseMotionListener(new MouseMotionAdapter(){public void mouseDragged(MouseEvent e){if(frame!=null&&drag[0]!=null){Point p=e.getLocationOnScreen();frame.setLocation(p.x-drag[0].x,p.y-drag[0].y);}}});
        }
        void geometry(){scale=Math.min(getWidth()/(double)CampusQuiz.WIDTH,getHeight()/(double)CampusQuiz.HEIGHT);ox=(getWidth()-CampusQuiz.WIDTH*scale)/2;oy=(getHeight()-CampusQuiz.HEIGHT*scale)/2;}
        public void doLayout(){geometry();for(Component child:getComponents()){Rectangle2D r=(Rectangle2D)((JComponent)child).getClientProperty("bounds");child.setBounds((int)Math.round(ox+r.getX()*scale),(int)Math.round(oy+r.getY()*scale),(int)Math.round(r.getWidth()*scale),(int)Math.round(r.getHeight()*scale));}}
        protected void paintComponent(Graphics graphics){super.paintComponent(graphics);geometry();Graphics2D g=(Graphics2D)graphics.create();g.translate(ox,oy);g.scale(scale,scale);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);draw(g,false);g.dispose();}
    }
    Font displayFont(String value,Font preferred,int size){if(preferred.canDisplayUpTo(value)<0)return preferred.deriveFont((float)size);Font fallback=new Font("Malgun Gothic",preferred.getStyle(),size);return fallback.canDisplayUpTo(value)<0?fallback:new Font(Font.DIALOG,preferred.getStyle(),size);}
    void text(Graphics2D g,String value,double x,double y,int size,Font font,Color color){g.setFont(displayFont(value,font,size));g.setColor(color);FontMetrics fm=g.getFontMetrics();int baseline=(int)y+fm.getAscent();for(String line:value.split("\n",-1)){g.drawString(line,(float)x,baseline);baseline+=(int)Math.round(size*1.45);}}
    void leading(Graphics2D g,String value,double x,double y,int size,int lead,Font f,Color color){int i=0;for(String line:value.split("\\n",-1))text(g,line,x,y+i++*lead,size,f,color);}
    void fitted(Graphics2D g,String value,double x,double y,int size,double width,Font font,Color color){Graphics2D copy=(Graphics2D)g.create();copy.setFont(font.deriveFont((float)size));double natural=copy.getFontMetrics().stringWidth(value);copy.translate(x,0);copy.scale(width/natural,1);text(copy,value,0,y,size,font,color);copy.dispose();}
    void paragraph(Graphics2D g,String value,int x,int y,int width,int size,Font font,Color color){
        Font layoutFont=displayFont(value,font,size);g.setFont(layoutFont);FontMetrics metrics=g.getFontMetrics();int row=0;
        for(String paragraph:value.split("\\n",-1)){
            StringBuilder line=new StringBuilder();
            for(String word:paragraph.trim().split("\\s+")){
                if(word.isEmpty())continue;
                String candidate=line.length()==0?word:line+" "+word;
                if(metrics.stringWidth(candidate)<=width){line.setLength(0);line.append(candidate);continue;}
                if(line.length()>0){text(g,line.toString(),x,y+row++*size*1.35,size,layoutFont,color);line.setLength(0);}
                if(metrics.stringWidth(word)<=width){line.append(word);continue;}
                for(char ch:word.toCharArray()){
                    if(metrics.stringWidth(line.toString()+ch)>width&&line.length()>0){text(g,line.toString(),x,y+row++*size*1.35,size,layoutFont,color);line.setLength(0);}
                    line.append(ch);
                }
            }
            if(line.length()>0)text(g,line.toString(),x,y+row++*size*1.35,size,layoutFont,color);
        }
    }
    void center(Graphics2D g,String str,double x,double y,double w,int size,Font font,Color color){g.setFont(font.deriveFont((float)size));double tw=g.getFontMetrics().stringWidth(str);text(g,str,x+(w-tw)/2,y,size,font,color);}
    void round(Graphics2D g,double x,double y,double w,double h,double r,Color fill,Color stroke){Shape s=new RoundRectangle2D.Double(x,y,w,h,r*2,r*2);if(fill!=null){g.setColor(fill);g.fill(s);}if(stroke!=null){g.setColor(stroke);g.setStroke(new BasicStroke(1));g.draw(s);}}
    void line(Graphics2D g,double x,double y,double w,Color c){g.setColor(c);g.setStroke(new BasicStroke(1));g.draw(new Line2D.Double(x,y,x+w,y));}
    void arrow(Graphics2D g,double x,double y,boolean right,Color color){g.setColor(color);g.setStroke(new BasicStroke(2));double d=right?1:-1;g.draw(new Line2D.Double(x-10*d,y,x+9*d,y));g.draw(new Line2D.Double(x+2*d,y-7,x+9*d,y));g.draw(new Line2D.Double(x+2*d,y+7,x+9*d,y));}
    void pill(Graphics2D g,boolean building,String label,int x,int y,int w,boolean filled,boolean enabled,boolean right,Runnable action){Color bg=!enabled?new Color(218,219,223):filled?INK:Color.WHITE;Color fg=!enabled?MUTED:filled?Color.WHITE:INK;round(g,x,y,w,66,33,bg,filled||!enabled?null:INK);g.setFont(bold.deriveFont(21f));double tw=g.getFontMetrics().stringWidth(label);if(right){double left=x+(w-tw-54)/2;text(g,label,left,y+19,21,bold,fg);arrow(g,left+tw+34,y+33,true,fg);}else center(g,label,x-(label.equals("이전 문제")?20:0),y+19,w,21,bold,fg);if(building)hit(label,x,y,w,66,enabled,action);}
    void link(Graphics2D g,boolean building,String label,int x,int y,int w,Runnable action){text(g,label,x,y,19,regular,MUTED);line(g,x,y+26,Math.min(w,regular.deriveFont(19f).getStringBounds(label,g.getFontRenderContext()).getWidth()),MUTED);if(building)hit(label,x,y,w,34,true,action);}
    void photo(Graphics2D g,String name,int x,int y,int w,int h,boolean gray){BufferedImage im=images.get(name);if(gray){ColorConvertOp op=new ColorConvertOp(java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_GRAY),null);im=op.filter(im,null);}g.drawImage(im,x,y,w,h,null);}
    void photoFit(Graphics2D g,String name,int x,int y,int w,int h){BufferedImage im=images.get(name);round(g,x,y,w,h,8,PALE,null);double scale=Math.min(w/(double)im.getWidth(),h/(double)im.getHeight());int drawW=(int)Math.round(im.getWidth()*scale),drawH=(int)Math.round(im.getHeight()*scale);g.drawImage(im,x+(w-drawW)/2,y+(h-drawH)/2,drawW,drawH,null);}
    void fade(Graphics2D g,int x,int y,int w,int h,boolean toWhite){g.setPaint(new GradientPaint(x,y,new Color(255,255,255,toWhite?0:255),x+w,y,new Color(255,255,255,toWhite?255:0)));g.fillRect(x,y,w,h);}
    void progress(Graphics2D g,int x,int y,int w,double fraction){round(g,x,y,w,7,4,LINE,null);round(g,x,y,w*fraction,7,4,INK,null);}
    void brand(Graphics2D g,int y){center(g,"KGU",0,y,1000,18,bold,INK);center(g,"KYONGGI UNIVERSITY",0,y+25,1000,12,regular,INK);}
    String partLabel(){return "PART 0"+(part+1)+"  "+PARTS[part];}
    void backbar(Graphics2D g,boolean b,String label,String meta,Runnable action){arrow(g,70,110,false,INK);text(g,label,94,92,21,bold,INK);text(g,meta,840-meta.length()*3,96,18,regular,MUTED);if(b)hit(label,52,83,350,45,true,action);}
    void checkIcon(Graphics2D g,int x,int y,int size,boolean correct){g.setColor(correct?INK:RED);g.fillOval(x,y,size,size);g.setColor(Color.WHITE);g.setStroke(new BasicStroke(2));if(correct){g.draw(new Line2D.Double(x+size*.30,y+size*.51,x+size*.44,y+size*.65));g.draw(new Line2D.Double(x+size*.44,y+size*.65,x+size*.70,y+size*.36));}else{g.draw(new Line2D.Double(x+size*.35,y+size*.35,x+size*.65,y+size*.65));g.draw(new Line2D.Double(x+size*.65,y+size*.35,x+size*.35,y+size*.65));}}
    void titlebar(Graphics2D g,boolean b){g.setColor(new Color(243,244,246));g.fillRect(0,0,1000,40);line(g,0,40,1000,LINE);g.setColor(INK);g.fillOval(18,9,22,22);center(g,"KGU",18,13,22,8,bold,Color.WHITE);text(g,"KGU QUIZ",51,10,17,regular,INK);g.setStroke(new BasicStroke(1.5f));g.setColor(INK);g.drawLine(870,22,880,22);g.drawRect(919,13,12,12);g.drawLine(970,16,979,25);g.drawLine(979,16,970,25);if(b){hit("최소화",850,0,50,40,true,()->{if(frame!=null)frame.setState(Frame.ICONIFIED);});hit("창 크기 변경",900,0,50,40,true,()->{if(frame!=null)frame.setExtendedState(frame.getExtendedState()==Frame.MAXIMIZED_BOTH?Frame.NORMAL:Frame.MAXIMIZED_BOTH);});hit("창 닫기",950,0,50,40,true,()->{if(frame!=null)frame.dispose();});}}
    void draw(Graphics2D g,boolean b){g.setColor(Color.WHITE);g.fillRect(0,0,1000,1080);switch(screen){case WELCOME->welcome(g,b);case GUIDE->guide(g,b);case PARTS->parts(g,b);case INTRO->intro(g,b);case QUESTION,SELECTED->question(g,b);case CORRECT,INCORRECT->feedback(g,b);case EXIT->{question(g,false);exitModal(g,b);}case PART_DONE->partDone(g,b);case FINAL->finalResult(g,b);case REVIEW->reviewActual(g,b);case REVIEW_DETAIL->reviewDetailActual(g,b);}titlebar(g,b);}
    void welcome(Graphics2D g,boolean b){
        photo(g,"welcome-campus",300,40,700,1040,false);fade(g,300,40,400,1040,false);g.setColor(INK);g.fillRect(0,60,21,160);
        text(g,"KYONGGI\nUNIVERSITY",70,80,24,bold,Color.BLACK);text(g,"NEW STUDENT\nQUIZ PROGRAM",760,81,22,bold,Color.BLACK);
        fitted(g,"KGU",70,208,82,170,black,Color.BLACK);fitted(g,"CAMPUS",70,293,82,352,black,Color.BLACK);fitted(g,"LIFE",70,380,82,164,black,Color.BLACK);g.setColor(INK);g.fillRect(242,400,238,89);fitted(g,"QUIZ.",251,380,82,218,black,Color.WHITE);
        text(g,"경기대학교 새내기 퀴즈",70,501,29,bold,Color.BLACK);line(g,369,518,100,INK);
        photo(g,"good-start",680,245,190,175,false);
        text(g,"퀴즈로 알아가는\n더 멋진 대학생활의 시작",70,646,29,regular,INK);photo(g,"start-script",235,780,230,110,false);
        pill(g,b,"퀴즈 시작하기",70,940,535,true,true,true,()->show(Screen.PARTS));center(g,"처음이라면? 이용 안내",70,1021,535,20,regular,INK);line(g,254,1046,167,INK);if(b)hit("처음이라면? 이용 안내",220,1015,250,45,true,()->show(Screen.GUIDE));
    }
    void guide(Graphics2D g,boolean b){
        backbar(g,b,"이용 안내","HOW TO PLAY",()->show(Screen.WELCOME));text(g,"시작 전, 잠깐 알아볼까요?",58,150,54,black,INK);text(g,"퀴즈로 만나는 경기대학교, 가볍게 풀고 새롭게 알아가세요.",58,232,22,regular,MUTED);
        round(g,58,292,884,122,10,PALE,null);String[] vals={"3개",totalQuestionCount()+"문항","10점",(totalQuestionCount()*10)+"점"},labels={"퀴즈 파트","총 문항 수","정답 1개당","총점"};for(int i=0;i<4;i++){center(g,vals[i],80+i*220,314,190,38,black,Color.BLACK);center(g,labels[i],80+i*220,367,190,19,regular,MUTED);}
        String[] heads={"원하는 파트를 선택해요","답을 고르고 해설을 확인해요","결과를 확인하고 오답을 복습해요"};String[] desc={"캠퍼스 안내와 학사·학습생활, 건물 사진 퀴즈까지\n세 가지 주제를 차례로 만나보세요.","문항마다 하나의 답변을 선택해 주세요.\n정답과 해설로 학교생활 정보를 익힐 수 있어요.","세 파트를 모두 마치면 최종 결과가 나와요.\n틀린 문제는 모아서 다시 살펴볼 수 있어요."};for(int i=0;i<3;i++){int y=444+i*130;text(g,"0"+(i+1),58,y,44,black,Color.BLACK);text(g,heads[i],148,y+2,27,bold,INK);text(g,desc[i],148,y+45,22,regular,MUTED);}
        line(g,58,840,884,LINE);text(g,"ⓘ  오답은 0점이며 감점은 없어요. 정답률과 소요 시간은 최종 결과에서 확인할 수 있어요.",58,868,19,regular,MUTED);pill(g,b,"확인했어요 · 파트 선택하기",58,928,884,true,true,true,()->show(Screen.PARTS));
    }
    void parts(Graphics2D g,boolean b){
        text(g,"퀴즈 파트를 선택하세요.",48,83,27,bold,INK);leading(g,"CHOOSE\nA PART.",48,114,68,70,black,Color.BLACK);text(g,"각 파트는 경기대학교\n대학생활의 다른 이야기를 담고 있어요.",572,208,22,regular,INK);for(int i=0;i<3;i++)round(g,806+i*52,82,42,8,4,i==part?INK:LINE,null);
        for(int i=0;i<3;i++){int y=295+i*240;Shape old=g.getClip();g.clip(new RoundRectangle2D.Double(38,y,924,225,10,10));photo(g,new String[]{"part-campus","academic","building-convention","good-start","start-script","nice-work","trophy","clock","star","celebration"}[i],38,y,924,225,false);g.setPaint(new GradientPaint(38,y,new Color(0,0,0,195),962,y,new Color(0,0,0,95)));g.fillRect(38,y,924,225);g.setClip(old);text(g,"0"+(i+1),75,y+47,92,bold,Color.WHITE);leading(g,i==0?"KGU\nCAMPUS GUIDE":PARTS[i],244,y+(i==0?48:71),34,36,bold,Color.WHITE);text(g,TAGLINES[i],244,y+141,24,bold,Color.WHITE);g.setColor(Color.WHITE);g.fillOval(850,y+75,74,74);arrow(g,887,y+112,true,INK);final int n=i;if(b)hit(PARTS[i],38,y,924,225,true,()->startPart(n));}brand(g,1020);
    }
    void intro(Graphics2D g,boolean b){
        backbar(g,b,"파트 선택","PART 0"+(part+1)+" / 03",()->show(Screen.PARTS));Shape old=g.getClip();g.clip(new RoundRectangle2D.Double(58,156,884,345,10,10));photo(g,new String[]{"intro-campus","academic","building-convention","good-start","start-script","nice-work","trophy","clock","star","celebration"}[part],58,156,884,345,false);g.setColor(new Color(0,0,0,90));g.fillRect(58,156,884,345);g.setClip(old);text(g,"0"+(part+1),94,188,92,bold,Color.WHITE);leading(g,part==0?"KGU\nCAMPUS GUIDE":PARTS[part],94,298,50,54,bold,Color.WHITE);
        text(g,TAGLINES[part],58,528,43,black,INK);text(g,part==0?"처음 만나는 캠퍼스를 조금 더 가깝게.\n학교 소개와 캠퍼스 안내를 퀴즈로 살펴보세요.":part==1?"알아두면 든든한 학사·학습생활.\n학교생활에 필요한 정보를 퀴즈로 살펴보세요.":"건물 사진을 보고 이름을 골라 보세요.\n경기대학교 캠퍼스 건물을 알아봅니다.",58,600,24,regular,MUTED);
        int count=questionCount();text(g,"총 "+count+"문항",58,728,25,bold,INK);text(g,"정답당 10점",369,728,25,bold,INK);text(g,"최대 "+(count*10)+"점",679,728,25,bold,INK);line(g,58,816,884,LINE);text(g,"하나의 답변을 고른 뒤 ‘다음 문제’를 눌러 주세요.\n정답과 해설을 확인하며 차근차근 진행할 수 있어요.",58,850,21,regular,MUTED);pill(g,b,"Part 0"+(part+1)+" 시작하기",58,940,884,true,true,false,()->{selected=-1;questionIndex=0;partScore=0;show(Screen.QUESTION);});
    }
    String questionText(){return currentQuestion().prompt();}
    String[] options(){return currentQuestion().options();}
    int answerIndex(){return currentQuestion().answer();}
    String explanation(){return currentQuestion().explanation();}
    void question(Graphics2D g,boolean b){
        text(g,partLabel(),58,80,23,bold,INK);text(g,(questionIndex+1)+" / "+questionCount(),886,81,26,bold,INK);progress(g,58,140,884,(questionIndex+1)/5.0);
        if(part==2)photoFit(g,currentQuestion().image(),442,172,498,276);else{photo(g,"question-campus",442,172,498,276,false);fade(g,442,172,160,276,false);}text(g,"Q.",58,186,66,black,Color.BLACK);paragraph(g,questionText(),58,282,360,24,bold,Color.BLACK);
        for(int i=0;i<4;i++){int y=476+i*100;boolean active=selected==i;round(g,58,y,884,84,4,active?INK:Color.WHITE,active?null:LINE);Color fg=active?Color.WHITE:INK;text(g,""+(char)('A'+i),86,y+22,28,bold,fg);text(g,options()[i],138,y+22,27,bold,fg);g.setColor(active?Color.WHITE:MUTED);g.setStroke(new BasicStroke(2));g.drawOval(892,y+32,20,20);if(active)g.fillOval(900,y+40,4,4);final int n=i;if(b)hit("답변 "+(char)('A'+i)+": "+options()[i],58,y,884,84,true,()->choose(n));}
        pill(g,b,"이전 문제",58,899,310,false,true,false,()->show(Screen.INTRO));arrow(g,260,932,false,INK);pill(g,b,"다음 문제",592,899,350,true,selected>=0,true,()->show(selected==answerIndex()?Screen.CORRECT:Screen.INCORRECT));text(g,selected<0?"답변을 선택하면 다음 문제로 이동할 수 있어요.":"답변을 선택했어요. 다음 문제로 진행해 주세요.",58,989,18,regular,MUTED);link(g,b,"퀴즈 종료",878,989,70,this::exitQuiz);
    }
    void feedback(Graphics2D g,boolean b){
        boolean correct=screen==Screen.CORRECT;int answer=answerIndex();photo(g,"feedback-campus",0,40,510,1040,false);fade(g,0,40,510,1040,true);text(g,(questionIndex+1)+" / "+questionCount(),44,67,25,bold,Color.BLACK);text(g,partLabel(),673,69,20,bold,Color.BLACK);checkIcon(g,396,207,64,correct);text(g,correct?"정답입니다!":"아쉬워요!",480,204,48,black,Color.BLACK);
        int answerY=correct?300:482;if(!correct){text(g,"내가 선택한 답변",396,299,18,regular,RED);round(g,396,335,548,83,5,new Color(253,241,240),RED);text(g,""+(char)('A'+selected)+"   "+options()[selected],424,357,27,bold,INK);text(g,"정답",396,447,18,bold,INK);}
        round(g,396,answerY,548,90,5,BLUE,null);text(g,""+(char)('A'+answer)+"   "+options()[answer],420,answerY+27,27,bold,Color.BLACK);line(g,396,answerY+118,548,LINE);text(g,"⊙ 해설",396,answerY+146,23,regular,Color.BLACK);paragraph(g,explanation(),396,answerY+188,548,24,regular,INK);pill(g,b,"다음 문제",396,954,548,true,true,true,this::nextAfterFeedback);
    }
    void exitModal(Graphics2D g,boolean b){
        g.setColor(new Color(0,0,0,102));g.fillRect(0,40,1000,1040);round(g,170,308,660,478,10,Color.WHITE,null);arrow(g,235,371,true,INK);text(g,"×",762,343,29,regular,INK);if(b)hit("종료 확인 닫기",748,340,50,50,true,()->show(beforeExit));text(g,"퀴즈를 그만 풀까요?",216,413,37,black,INK);text(g,"지금 종료하면 진행 중인 Part 0"+(part+1)+"의 답변은 저장되지 않아요.\n파트 선택 화면으로 돌아갑니다.",216,490,23,regular,MUTED);round(g,216,585,568,62,6,PALE,null);text(g,"PART 0"+(part+1),236,603,19,bold,INK);text(g,(questionIndex+1)+" / "+questionCount()+" 문항 진행 중",628,603,18,regular,INK);pill(g,b,"종료하기",216,675,276,false,true,false,()->{selected=-1;show(Screen.PARTS);});pill(g,b,"계속 풀기",508,675,276,true,true,false,()->show(beforeExit));
    }
    void partDone(Graphics2D g,boolean b){
        checkIcon(g,464,90,72,true);center(g,"PART 0"+(part+1)+" · "+PARTS[part],0,191,1000,21,bold,INK);center(g,new String[]{"첫 번째 파트 완료!","두 번째 파트 완료!","세 번째 파트 완료!"}[part],0,230,1000,49,black,Color.BLACK);center(g,part==2?"사진 속 건물을 잘 맞혀 보셨나요?":"캠퍼스를 알아가는 첫걸음, 잘 마쳤어요.",0,305,1000,23,regular,MUTED);
        int count=questionCount();round(g,100,367,800,298,10,Color.WHITE,LINE);text(g,"파트 점수",132,394,23,regular,Color.BLACK);text(g,""+partScore,132,421,78,black,Color.BLACK);text(g,"/ "+(count*10),250,472,30,regular,Color.BLACK);text(g,"정답",508,444,22,regular,Color.BLACK);text(g,(partScore/10)+" / "+count,508,482,36,regular,Color.BLACK);text(g,"풀이 완료",790,444,22,regular,Color.BLACK);text(g,count+" / "+count,790,482,36,regular,Color.BLACK);line(g,132,547,736,LINE);text(g,"전체 진행 상황",132,575,21,regular,INK);text(g,(part+1)+" / 3 파트 완료",750,575,20,bold,INK);for(int i=0;i<3;i++)round(g,132+i*248,625,239,8,4,i<=part?INK:LINE,null);
        round(g,100,693,800,116,6,PALE,null);text(g,part<2?"0"+(part+2):"03",124,713,52,bold,Color.BLACK);text(g,part<2?PARTS[part+1]:"모든 파트를 마쳤어요!",220,717,24,bold,INK);text(g,part<2?"다음은 "+TAGLINES[part+1]:"최종 결과를 확인해 보세요.",220,754,21,regular,MUTED);arrow(g,864,751,true,INK);pill(g,b,part<2?"다음 파트 시작하기":"최종 결과 확인하기",100,837,800,true,true,true,this::nextPart);pill(g,b,"파트 선택으로",100,917,800,false,true,false,()->show(Screen.PARTS));brand(g,1010);
    }
    void finalResult(Graphics2D g,boolean b){
        int score=Arrays.stream(partScores).sum(),maximum=totalQuestionCount()*10,correct=score/10;double rate=score/(double)maximum;String percent=Math.round(rate*100)+"%";int wrong=totalQuestionCount()-correct;
        photo(g,"result-campus",630,40,370,1040,true);fade(g,630,40,290,1040,false);text(g,"☰",40,70,23,regular,INK);if(b)hit("파트 선택 메뉴",30,60,50,50,true,()->show(Screen.PARTS));text(g,"퀴즈 완료!",458,129,29,bold,INK);photo(g,"celebration",421,134,29,29,false);center(g,"최종 결과",0,170,1000,64,black,Color.BLACK);
        round(g,155,278,690,285,10,Color.WHITE,LINE);text(g,"총점",197,308,23,regular,Color.BLACK);text(g,""+score,197,351,108,black,Color.BLACK);text(g,"/ "+maximum,399,422,40,regular,Color.BLACK);text(g,"정답률 "+percent,399,477,26,regular,Color.BLACK);progress(g,197,524,606,rate);photo(g,"nice-work",740,235,170,145,false);
        String[] vals={correct+" / "+totalQuestionCount(),"04:32",rate==1.0?"KGU 마스터":"계속 도전"},labels={"정답","소요 시간","결과"};for(int i=0;i<3;i++){int cx=265+i*234;g.setColor(Color.WHITE);g.fillOval(cx-39,590,78,78);g.setColor(INK);g.setStroke(new BasicStroke(1.5f));g.drawOval(cx-39,590,78,78);photo(g,new String[]{"trophy","clock","star"}[i],cx-24,605,48,47,false);center(g,labels[i],cx-100,681,200,23,regular,INK);center(g,vals[i],cx-110,718,220,26,regular,Color.BLACK);}
        center(g,rate==1.0?"이제 당신은 KGU 마스터!":"퀴즈를 모두 마쳤어요!",0,777,1000,27,bold,INK);center(g,"새롭게 알게 된 정보와 함께 캠퍼스 생활을 시작해 보세요.",0,826,1000,21,regular,MUTED);center(g,wrong==0?"모든 문제를 맞혔어요!":"틀린 "+wrong+"문제도 다시 확인해 보세요.",0,856,1000,21,regular,MUTED);pill(g,b,wrong==0?"최종 결과 확인하기":"오답 "+wrong+"문제 복습하기",155,891,690,true,true,true,()->show(Screen.REVIEW));pill(g,b,"다시 도전하기",38,975,372,false,true,false,()->startPart(0));pill(g,b,"처음으로",590,975,372,true,true,false,this::reset);
        g.setColor(INK);for(int[] r:new int[][]{{190,170},{105,238},{122,452},{75,713},{887,691},{896,214}}){AffineTransform t=g.getTransform();g.rotate(.55,r[0],r[1]);g.fillRect(r[0],r[1],6,22);g.setTransform(t);}
    }
    void reviewActual(Graphics2D g,boolean b){
        backbar(g,b,"최종 결과","REVIEW",()->show(Screen.FINAL));int total=missed.size();text(g,"오답 복습",58,155,54,black,Color.BLACK);text(g,"틀린 문제를 다시 살펴보고, 정답과 해설을 확인해 보세요.",58,234,22,regular,MUTED);text(g,"총 "+total+"문제",858,235,23,regular,Color.BLACK);
        int score=Arrays.stream(partScores).sum(),correct=score/10;round(g,58,292,884,66,6,PALE,null);text(g,totalQuestionCount()+"문항 중 "+correct+"문항 정답 · 오답 "+total+"문제",82,311,21,regular,INK);
        if(total==0){round(g,58,385,884,300,10,Color.WHITE,LINE);center(g,"복습할 오답이 없습니다.",58,500,884,30,bold,INK);pill(g,b,"최종 결과로 돌아가기",58,989,884,false,true,false,()->show(Screen.FINAL));return;}
        int pages=(total+2)/3;reviewPage=Math.min(reviewPage,pages-1);int start=reviewPage*3,end=Math.min(start+3,total);for(int i=start;i<end;i++){int y=385+(i-start)*202;MissedQuestion m=missed.get(i);QuizQuestion q=m.question();round(g,58,y,884,182,10,Color.WHITE,LINE);text(g,"PART 0"+(m.part()+1)+" · Q. "+m.number()+" · "+PARTS[m.part()],82,y+16,17,bold,MUTED);paragraph(g,q.prompt(),82,y+44,810,21,regular,Color.BLACK);text(g,"내 답: "+q.options()[m.selected()],82,y+119,18,regular,RED);text(g,"정답: "+q.options()[q.answer()],512,y+119,18,bold,INK);arrow(g,907,y+78,true,INK);final int index=i;if(b)hit("오답 복습 "+(i+1),58,y,884,182,true,()->{reviewIndex=index;show(Screen.REVIEW_DETAIL);});}
        if(pages>1){pill(g,b,"이전",58,989,260,false,reviewPage>0,false,()->{reviewPage--;show(Screen.REVIEW);});center(g,(reviewPage+1)+" / "+pages,380,1006,240,20,bold,INK);pill(g,b,"다음",682,989,260,true,reviewPage+1<pages,false,()->{reviewPage++;show(Screen.REVIEW);});}
        else pill(g,b,"최종 결과로 돌아가기",58,989,884,false,true,false,()->show(Screen.FINAL));
    }
    void reviewDetailActual(Graphics2D g,boolean b){
        if(missed.isEmpty()){show(Screen.REVIEW);return;}MissedQuestion m=missed.get(reviewIndex);QuizQuestion q=m.question();backbar(g,b,"오답 복습 목록","REVIEW "+(reviewIndex+1)+" / "+missed.size(),()->show(Screen.REVIEW));text(g,"PART 0"+(m.part()+1)+" · "+PARTS[m.part()],58,132,21,bold,INK);text(g,"Q. "+m.number(),870,132,21,bold,Color.BLACK);paragraph(g,q.prompt(),58,180,884,32,black,Color.BLACK);text(g,"내가 선택한 답과 정답을 비교해 보세요.",58,333,20,regular,MUTED);
        round(g,58,382,884,112,5,Color.WHITE,LINE);text(g,"내가 선택한 답변",82,401,20,regular,RED);text(g,""+(char)('A'+m.selected())+"   "+q.options()[m.selected()],82,438,27,bold,Color.BLACK);round(g,58,510,884,112,5,BLUE,null);text(g,"정답",82,529,20,bold,INK);text(g,""+(char)('A'+q.answer())+"   "+q.options()[q.answer()],82,566,27,regular,Color.BLACK);line(g,58,650,884,LINE);text(g,"해설",58,678,25,bold,INK);paragraph(g,q.explanation(),58,720,884,22,regular,INK);
        pill(g,b,"복습 목록으로",58,936,430,false,true,false,()->show(Screen.REVIEW));pill(g,b,"다음 오답 보기",510,936,432,true,true,true,()->{reviewIndex=(reviewIndex+1)%missed.size();reviewPage=reviewIndex/3;show(Screen.REVIEW_DETAIL);});
    }
    void review(Graphics2D g,boolean b){
        backbar(g,b,"최종 결과","REVIEW",()->show(Screen.FINAL));text(g,"오답 복습",58,155,54,black,Color.BLACK);text(g,"틀린 문제를 다시 살펴보고, 배운 내용을 정리해 보세요.",58,234,22,regular,MUTED);text(g,"총 3문제",858,235,23,regular,Color.BLACK);round(g,58,292,884,66,6,PALE,null);text(g,"15문항 중 12문항 정답 · 정답률 80%",82,311,21,regular,INK);text(g,"복습할 문제 3",808,310,19,bold,INK);
        String[] qs={"KGU소개에서 확인할 수 있는 내용은?","학사·학습생활을 알아보는 퀴즈 파트는?","비교과 프로그램을 알아보는 퀴즈 파트는?"};for(int i=0;i<3;i++){int y=385+i*202;round(g,58,y,884,182,10,Color.WHITE,LINE);text(g,"PART 0"+(i+1)+" · "+PARTS[i],82,y+20,16,bold,MUTED);text(g,"Q. "+new String[]{"03","08","13"}[i],870,y+20,19,bold,Color.BLACK);text(g,qs[i],82,y+60,25,regular,Color.BLACK);arrow(g,907,y+77,true,INK);text(g,"내 답변",82,y+108,16,regular,MUTED);text(g,"정답",512,y+108,16,regular,MUTED);text(g,new String[]{OPTIONS[1],PARTS[0],PARTS[1]}[i],82,y+138,18,regular,INK);text(g,new String[]{OPTIONS[0],PARTS[1],PARTS[2]}[i],512,y+138,18,bold,INK);final int n=i;if(b)hit("오답 복습 "+(i+1),58,y,884,182,true,()->{reviewIndex=n;show(Screen.REVIEW_DETAIL);});}pill(g,b,"최종 결과로 돌아가기",58,989,884,false,true,false,()->show(Screen.FINAL));
    }
    void reviewDetail(Graphics2D g,boolean b){
        backbar(g,b,"오답 복습 목록","REVIEW "+(reviewIndex+1)+" / 3",()->show(Screen.REVIEW));text(g,"PART 0"+(reviewIndex+1)+" · "+PARTS[reviewIndex],58,132,21,bold,INK);text(g,"Q. "+new String[]{"03","08","13"}[reviewIndex],870,132,21,bold,INK);leading(g,reviewIndex==0?"경기대학교 입학처 홈페이지의\nKGU소개에서 확인할 수 있는 내용으로\n알맞은 것은?":reviewIndex==1?"학사·학습생활을 알아보는\n퀴즈 파트는?":"비교과 프로그램을 알아보는\n퀴즈 파트는?",58,176,40,48,black,Color.BLACK);text(g,"내가 선택했던 답과 정답을 비교해 보세요.",58,341,20,regular,MUTED);
        round(g,58,396,884,112,5,Color.WHITE,LINE);text(g,"⊗ 내가 선택한 답변",82,416,20,regular,RED);text(g,"B   "+new String[]{OPTIONS[1],PARTS[0],PARTS[1]}[reviewIndex],82,453,27,bold,Color.BLACK);round(g,58,524,884,112,5,BLUE,null);text(g,"⊙ 정답",82,544,20,bold,INK);text(g,"A   "+new String[]{OPTIONS[0],PARTS[1],PARTS[2]}[reviewIndex],82,581,27,regular,Color.BLACK);line(g,58,664,884,LINE);text(g,"해설",58,696,25,bold,INK);text(g,reviewIndex==0?"경기대학교 입학처의 KGU소개 메뉴에서는 캠퍼스 안내(VR 캠퍼스)를 통해 온라인으로 학교\n캠퍼스를 둘러볼 수 있습니다.":"이 파트는 "+TAGLINES[reviewIndex]+"\n학교 안내에서 관련 정보를 확인해 보세요.",58,748,24,regular,INK);text(g,"기억해 두세요 · KGU소개는 학교와 캠퍼스를 알아보는 메뉴예요.\n개인별 출석 점수를 확인하는 메뉴와는 구분해 주세요.",58,832,21,regular,MUTED);pill(g,b,"복습 목록으로",58,936,430,false,true,false,()->show(Screen.REVIEW));pill(g,b,"다음 오답 보기",510,936,432,true,true,true,()->{reviewIndex=(reviewIndex+1)%3;show(Screen.REVIEW_DETAIL);});
    }
    void exportPreviews()throws IOException{File dir=new File("previews/pdf-design");dir.mkdirs();int i=0;for(Screen s:Screen.values()){part=0;reviewIndex=0;selected=s==Screen.QUESTION?-1:s==Screen.INCORRECT?1:0;screen=s;rebuild();root.setSize(WIDTH,HEIGHT);root.doLayout();BufferedImage out=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);Graphics2D g=out.createGraphics();root.printAll(g);g.dispose();ImageIO.write(out,"png",new File(dir,String.format("%02d-%s.png",++i,s.name().toLowerCase())));}System.out.println("Rendered 13 PDF-reference screens to previews/pdf-design/");}
    public static void main(String[] args)throws Exception{boolean preview=Arrays.asList(args).contains("--preview");if(preview)System.setProperty("java.awt.headless","true");loadFonts();SwingUtilities.invokeAndWait(()->{CampusQuiz app=new CampusQuiz();if(preview){try{app.exportPreviews();}catch(IOException e){throw new RuntimeException(e);}return;}app.frame=new JFrame("KGU QUIZ · UI Demo");app.frame.setUndecorated(true);app.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);app.frame.setContentPane(app.root);app.frame.pack();app.frame.setMinimumSize(new Dimension(600,648));app.frame.setLocationRelativeTo(null);app.frame.setVisible(true);});}
}
