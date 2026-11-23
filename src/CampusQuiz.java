import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Interactive Swing presentation of the ref.pdf screens, with Campus Rain, plate-shoot, and street-view demos. */
public class CampusQuiz {
    enum Screen { WELCOME, GUIDE, PARTS, INTRO, QUESTION, SELECTED, PLATE_GAME, CORRECT, INCORRECT, EXIT, PART_DONE, STREET_GAME, STREET_FOUND, FINAL, REVIEW, REVIEW_DETAIL }
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
    int part=0, selected=-1, reviewIndex=0, questionIndex=0, partScore=0, plateRound=0, streetPosition=1;
    int[] partScores={0,0,0};
    final java.util.List<MissedQuestion> missed=new ArrayList<>();
    int reviewPage=0;
    JFrame frame;
    record Hotspot(String name,Rectangle2D.Double bounds,Runnable action,boolean enabled){}
    final java.util.List<Hotspot> hotspots=new ArrayList<>();
    static final class FlyingPlate {
        int id, factIndex;
        double x,y,vx,vy,gravity;
        FlyingPlate(int id,int factIndex,double x,double y,double vx,double vy,double gravity){this.id=id;this.factIndex=factIndex;this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.gravity=gravity;}
    }
    static final class ShotEffect { double x,y,expires; boolean hit,correct; ShotEffect(double x,double y,double expires,boolean hit,boolean correct){this.x=x;this.y=y;this.expires=expires;this.hit=hit;this.correct=correct;} }
    final java.util.List<FlyingPlate> flyingPlates=new ArrayList<>();
    final java.util.List<ShotEffect> shotEffects=new ArrayList<>();
    final Random plateRandom=new Random();
    javax.swing.Timer plateTimer;
    int nextPlateId=1, nextSpawnMs=850, spawnOptionCursor=0;
    double aimX=500, aimY=520;
    boolean pendingPlateOutcome=false, outcomeCorrect=false;

    static InputStream resource(String path)throws IOException{InputStream in=CampusQuiz.class.getResourceAsStream("/"+path);return in!=null?in:new FileInputStream(path);}
    static Font loadFont(String name,String fallback,int style){try(InputStream in=resource("assets/fonts/"+name)){return Font.createFont(Font.TRUETYPE_FONT,in);}catch(Exception e){return new Font(fallback,style,20);}}
    static void loadFonts(){regular=loadFont("NotoSansKR-Regular.otf","Noto Sans CJK KR",Font.PLAIN);bold=loadFont("NotoSansKR-Bold.otf","Noto Sans CJK KR",Font.BOLD);black=loadFont("NotoSansKR-Black.otf","Noto Sans CJK KR Black",Font.BOLD);}
    CampusQuiz(){for(String n:new String[]{"welcome-campus","part-campus","intro-campus","question-campus","feedback-campus","result-campus","academic","program","good-start","start-script","nice-work","trophy","clock","star","celebration","building-first-lecture","building-library","building-convention"})try(InputStream in=resource("assets/images/"+n+".png")){BufferedImage im=ImageIO.read(in);if(im==null)throw new IOException(n);images.put(n,im);}catch(IOException e){throw new IllegalStateException("Missing design asset: "+n,e);}for(int i=1;i<=13;i++)loadPhoto("street-"+i+".jpg");loadPhoto("street-answer.jpg");show(Screen.WELCOME);}
    void loadPhoto(String filename){try(InputStream in=resource("assets/images/"+filename)){BufferedImage im=ImageIO.read(in);if(im==null)throw new IOException(filename);images.put(filename,im);}catch(IOException e){throw new IllegalStateException("Missing street-view asset: "+filename,e);}}
    void show(Screen next){if(screen==Screen.PLATE_GAME&&next!=Screen.PLATE_GAME)stopPlateTimer();screen=next;rebuild();if(next==Screen.PLATE_GAME)startPlateTimer();}
    void choose(int index){selected=index;show(Screen.SELECTED);}
    void startPart(int index){part=index;selected=-1;questionIndex=0;plateRound=0;partScore=0;partScores[index]=0;missed.removeIf(m->m.part()==index);show(Screen.INTRO);}
    QuizQuestion currentQuestion(){return part==0?CAMPUS_QUIZ[questionIndex]:part==1?ACADEMIC_QUIZ[questionIndex]:BUILDING_QUIZ[questionIndex];}
    int questionCount(){return part==0?CAMPUS_QUIZ.length:part==1?ACADEMIC_QUIZ.length:BUILDING_QUIZ.length;}
    int totalQuestionCount(){return CAMPUS_QUIZ.length+ACADEMIC_QUIZ.length+BUILDING_QUIZ.length;}
    void nextAfterFeedback(){if(screen==Screen.CORRECT)partScore+=10;else missed.add(new MissedQuestion(part,questionIndex+1,currentQuestion(),selected));selected=-1;questionIndex++;if(questionIndex<questionCount())show(Screen.QUESTION);else{partScores[part]=partScore;show(Screen.PART_DONE);}}
    void reset(){part=0;selected=-1;reviewIndex=0;reviewPage=0;questionIndex=0;plateRound=0;partScore=0;streetPosition=1;pendingPlateOutcome=false;outcomeCorrect=false;flyingPlates.clear();shotEffects.clear();Arrays.fill(partScores,0);missed.clear();show(Screen.WELCOME);}
    void startPlateGame(){selected=-1;questionIndex=0;plateRound=0;partScore=0;pendingPlateOutcome=false;outcomeCorrect=false;flyingPlates.clear();shotEffects.clear();nextPlateId=1;nextSpawnMs=850;spawnOptionCursor=0;aimX=500;aimY=520;show(Screen.PLATE_GAME);}
    void shootPlate(int target){
        if(part!=1||screen!=Screen.PLATE_GAME)return;
        if(pendingPlateOutcome)return;
        FlyingPlate hit=null;for(FlyingPlate p:flyingPlates)if(p.id==target){hit=p;break;}if(hit==null)return;
        flyingPlates.remove(hit);
        outcomeCorrect=hit.factIndex==currentQuestion().answer();pendingPlateOutcome=true;plateRound++;if(outcomeCorrect)partScore+=10;else missed.add(new MissedQuestion(part,questionIndex+1,currentQuestion(),hit.factIndex));
        rebuild();
    }
    void startPlateTimer(){
        if(plateTimer==null){plateTimer=new javax.swing.Timer(33,e->tickPlateGame());plateTimer.setCoalesce(true);}
        if(!plateTimer.isRunning())plateTimer.start();
    }
    void stopPlateTimer(){if(plateTimer!=null)plateTimer.stop();}
    void spawnFlyingPlate(){
        boolean fromLeft=plateRandom.nextBoolean();double y=560+plateRandom.nextDouble()*100;double speed=7.0+plateRandom.nextDouble()*2.2;
        double x=fromLeft?-92:1092;double vx=fromLeft?speed:-speed;double vy=-(2.2+plateRandom.nextDouble()*.8);int fact=spawnOptionCursor++%4;
        flyingPlates.add(new FlyingPlate(nextPlateId++,fact,x,y,vx,vy,.038+plateRandom.nextDouble()*.01));
    }
    void spawnPreviewPlates(){
        flyingPlates.clear();nextPlateId=1;
        spawnOptionCursor=0;
        flyingPlates.add(new FlyingPlate(nextPlateId++,0,245,470,7.2,-.8,.042));
        flyingPlates.add(new FlyingPlate(nextPlateId++,1,552,555,-8.0,-.35,.042));
        flyingPlates.add(new FlyingPlate(nextPlateId++,2,815,430,-7.0,.55,.042));
        flyingPlates.add(new FlyingPlate(nextPlateId++,3,720,620,-7.5,-.2,.042));
    }
    void tickPlateGame(){
        if(screen!=Screen.PLATE_GAME)return;
        double now=System.currentTimeMillis();
        if(pendingPlateOutcome){shotEffects.removeIf(s->s.expires<=now);rebuild();return;}
        for(FlyingPlate p:flyingPlates){p.x+=p.vx;p.y+=p.vy;p.vy+=p.gravity;}
        flyingPlates.removeIf(p->p.x<-180||p.x>1180||p.y>880);
        nextSpawnMs-=33;if(nextSpawnMs<=0){if(flyingPlates.size()<4)spawnFlyingPlate();nextSpawnMs=430+plateRandom.nextInt(600);}
        shotEffects.removeIf(s->s.expires<=now);
        rebuild();
    }
    void resolvePlateOutcome(){
        if(!pendingPlateOutcome||screen!=Screen.PLATE_GAME)return;
        pendingPlateOutcome=false;questionIndex++;flyingPlates.clear();spawnOptionCursor=0;nextSpawnMs=850;
        if(plateRound>=questionCount()){partScores[part]=partScore;show(Screen.PART_DONE);}else rebuild();
    }
    double plateVerticalScale(FlyingPlate p){return .68;}
    void fireAt(double x,double y){
        if(screen!=Screen.PLATE_GAME||pendingPlateOutcome)return;aimX=Math.max(58,Math.min(942,x));aimY=Math.max(190,Math.min(828,y));
        FlyingPlate hit=null;double nearest=Double.MAX_VALUE;
        for(FlyingPlate p:flyingPlates){double dx=x-p.x,dy=y-p.y,scale=plateVerticalScale(p),distance=(dx/80)*(dx/80)+(dy/(38*scale))*(dy/(38*scale));if(distance<=1.0&&distance<nearest){nearest=distance;hit=p;}}
        boolean correct=hit!=null&&hit.factIndex==currentQuestion().answer();shotEffects.add(new ShotEffect(aimX,aimY,System.currentTimeMillis()+150,hit!=null,correct));
        if(hit!=null)shootPlate(hit.id);else rebuild();
    }
    void nextPart(){if(part<2)startPart(part+1);else if(screen==Screen.STREET_FOUND)show(Screen.FINAL);else{streetPosition=1;show(Screen.STREET_GAME);}}
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
            Point[] drag={null};addMouseListener(new MouseAdapter(){public void mousePressed(MouseEvent e){double logicalY=(e.getY()-oy)/scale;if(screen==Screen.PLATE_GAME&&SwingUtilities.isLeftMouseButton(e)&&logicalY>=40){updateAim(e);fireAt(aimX,aimY);return;}if(frame!=null&&logicalY<40)drag[0]=e.getPoint();}public void mouseReleased(MouseEvent e){drag[0]=null;}});addMouseMotionListener(new MouseMotionAdapter(){public void mouseMoved(MouseEvent e){if(screen==Screen.PLATE_GAME)updateAim(e);}public void mouseDragged(MouseEvent e){if(screen==Screen.PLATE_GAME)updateAim(e);if(frame!=null&&drag[0]!=null){Point p=e.getLocationOnScreen();frame.setLocation(p.x-drag[0].x,p.y-drag[0].y);}}});
        }
        void updateAim(MouseEvent e){geometry();aimX=Math.max(58,Math.min(942,(e.getX()-ox)/scale));aimY=Math.max(190,Math.min(828,(e.getY()-oy)/scale));repaint();}
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
    void draw(Graphics2D g,boolean b){g.setColor(Color.WHITE);g.fillRect(0,0,1000,1080);switch(screen){case WELCOME->welcome(g,b);case GUIDE->guide(g,b);case PARTS->parts(g,b);case INTRO->intro(g,b);case QUESTION,SELECTED->question(g,b);case PLATE_GAME->plateGame(g,b);case CORRECT,INCORRECT->feedback(g,b);case EXIT->{if(part==1&&beforeExit==Screen.PLATE_GAME)plateGame(g,false);else if(part==2&&beforeExit==Screen.STREET_GAME)streetGame(g,false);else question(g,false);exitModal(g,b);}case PART_DONE->partDone(g,b);case STREET_GAME->streetGame(g,b);case STREET_FOUND->streetFound(g,b);case FINAL->finalResult(g,b);case REVIEW->reviewActual(g,b);case REVIEW_DETAIL->reviewDetailActual(g,b);}titlebar(g,b);}
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
        String[] heads={"건물 이름을 입력해요","답을 고르고 해설을 확인해요","사진 속 건물을 맞혀요"};String[] desc={"떨어지는 건물 이름을 입력해 캠퍼스를 익혀 보세요.\n첫 번째 선택지는 소나기 미니게임으로 열립니다.","학사·학습생활 문제를 풀고,\n두 번째 파트는 날아오는 접시를 클릭해요.","건물 사진을 보고 이름을 고른 뒤\n거리뷰 탐색으로 정답 장소를 찾아요."};for(int i=0;i<3;i++){int y=444+i*130;text(g,"0"+(i+1),58,y,44,black,Color.BLACK);text(g,heads[i],148,y+2,27,bold,INK);text(g,desc[i],148,y+45,22,regular,MUTED);}
        line(g,58,840,884,LINE);text(g,"ⓘ  오답은 0점이며 감점은 없어요. 정답률과 소요 시간은 최종 결과에서 확인할 수 있어요.",58,868,19,regular,MUTED);pill(g,b,"확인했어요 · 파트 선택하기",58,928,884,true,true,true,()->show(Screen.PARTS));
    }
    void parts(Graphics2D g,boolean b){
        text(g,"게임 파트를 선택하세요.",48,83,27,bold,INK);leading(g,"CHOOSE\nA GAME.",48,114,68,70,black,Color.BLACK);text(g,"각 게임은 경기대학교\n대학생활의 다른 이야기를 담고 있어요.",572,208,22,regular,INK);for(int i=0;i<3;i++)round(g,806+i*52,82,42,8,4,i==part?INK:LINE,null);
        for(int i=0;i<3;i++){int y=295+i*240;Shape old=g.getClip();g.clip(new RoundRectangle2D.Double(38,y,924,225,10,10));photo(g,new String[]{"part-campus","academic","building-convention"}[i],38,y,924,225,false);g.setPaint(new GradientPaint(38,y,new Color(0,0,0,195),962,y,new Color(0,0,0,95)));g.fillRect(38,y,924,225);g.setClip(old);text(g,"0"+(i+1),75,y+47,92,bold,Color.WHITE);String cardTitle=i==0?"KGU\nCAMPUS RAIN":PARTS[i];leading(g,cardTitle,244,y+(i==0?48:71),34,36,bold,Color.WHITE);String cardTagline=i==0?"떨어지는 건물 이름을 입력해요":TAGLINES[i];text(g,cardTagline,244,y+141,24,bold,Color.WHITE);g.setColor(Color.WHITE);g.fillOval(850,y+75,74,74);arrow(g,887,y+112,true,INK);final int n=i;String actionName=i==0?"KGU CAMPUS RAIN":PARTS[i];if(b)hit(actionName,38,y,924,225,true,i==0?CampusRain::openWindow:()->startPart(n));}brand(g,1020);
    }
    void intro(Graphics2D g,boolean b){
        backbar(g,b,"파트 선택","PART 0"+(part+1)+" / 03",()->show(Screen.PARTS));Shape old=g.getClip();g.clip(new RoundRectangle2D.Double(58,156,884,345,10,10));photo(g,new String[]{"intro-campus","academic","building-convention","good-start","start-script","nice-work","trophy","clock","star","celebration"}[part],58,156,884,345,false);g.setColor(new Color(0,0,0,90));g.fillRect(58,156,884,345);g.setClip(old);text(g,"0"+(part+1),94,188,92,bold,Color.WHITE);leading(g,part==0?"KGU\nCAMPUS GUIDE":PARTS[part],94,298,50,54,bold,Color.WHITE);
        text(g,TAGLINES[part],58,528,43,black,INK);text(g,part==0?"처음 만나는 캠퍼스를 조금 더 가깝게.\n학교 소개와 캠퍼스 안내를 퀴즈로 살펴보세요.":part==1?"알아두면 든든한 학사·학습생활.\n날아오는 접시를 쏘며 핵심 정보를 모아 보세요.":"건물 사진을 보고 이름을 골라 보세요.\n경기대학교 캠퍼스 건물을 알아봅니다.",58,600,24,regular,MUTED);
        int count=questionCount();String unit=part==1?"접시":"문항";text(g,"총 "+count+unit,58,728,25,bold,INK);text(g,part==1?"성공당 10점":"정답당 10점",369,728,25,bold,INK);text(g,"최대 "+(count*10)+"점",679,728,25,bold,INK);line(g,58,816,884,LINE);text(g,part==1?"날아오는 접시를 클릭해 명중시키세요.\n다섯 번 성공하면 파트가 완료됩니다.":"하나의 답변을 고른 뒤 ‘다음 문제’를 눌러 주세요.\n정답과 해설을 확인하며 차근차근 진행할 수 있어요.",58,850,21,regular,MUTED);pill(g,b,"Part 0"+(part+1)+" 시작하기",58,940,884,true,true,false,()->{selected=-1;questionIndex=0;partScore=0;if(part==1)startPlateGame();else show(Screen.QUESTION);});
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
    void plateGame(Graphics2D g,boolean b){
        Object oldAA=g.getRenderingHint(RenderingHints.KEY_ANTIALIASING),oldTextAA=g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Color arcadeBg=new Color(8,12,28),arcadePanel=new Color(12,19,43),arcadeCyan=new Color(61,240,255),arcadePink=new Color(255,68,190),arcadeYellow=new Color(255,224,91);g.setColor(arcadeBg);g.fillRect(0,40,1000,1040);
        text(g,"ACADEMIC LIFE // PLATE SHOOT",58,76,21,bold,arcadeCyan);text(g,"STAGE 02",58,111,16,bold,arcadePink);text(g,"SCORE "+String.format("%03d",partScore),730,111,18,bold,arcadeYellow);arcadeProgress(g,58,135,884,plateRound/(double)questionCount(),arcadeCyan);
        g.setColor(arcadePanel);g.fillRect(58,175,884,665);g.setColor(new Color(26,56,91));g.setStroke(new BasicStroke(2));g.drawRect(58,175,884,665);g.setColor(new Color(61,240,255,35));for(int x=78;x<942;x+=32)g.drawLine(x,175,x,840);for(int y=191;y<840;y+=32)g.drawLine(58,y,942,y);g.setColor(new Color(255,255,255,18));for(int y=180;y<840;y+=8)g.drawLine(58,y,942,y);
        text(g,"QUESTION "+(questionIndex+1)+" / "+questionCount(),86,211,18,bold,arcadePink);text(g,"정답 접시를 쏴라",752,211,18,bold,arcadeYellow);String[] options=currentQuestion().options();g.setColor(new Color(7,12,28,235));g.fillRect(74,242,852,188);g.setColor(new Color(61,240,255,110));g.drawRect(74,242,852,188);text(g,"정답에 맞는 접시를 쏴서 학사 지식을 모아 보세요",86,270,29,black,Color.WHITE);paragraph(g,currentQuestion().prompt(),86,312,790,23,bold,Color.WHITE);
        Shape playClip=g.getClip();g.clipRect(58,175,884,665);for(FlyingPlate p:flyingPlates)drawFlyingPlate(g,p,options[p.factIndex],arcadeCyan,arcadeYellow);g.setClip(playClip);
        if(flyingPlates.isEmpty()){text(g,"WAITING FOR TARGETS...",350,515,22,bold,arcadeYellow);text(g,"양쪽에서 보기 접시가 날아옵니다",330,550,18,regular,new Color(175,205,220));}
        drawShotEffects(g);drawAimAndBlaster(g,arcadeCyan,arcadeYellow);drawPlateOutcome(g,b,arcadeCyan,arcadePink);text(g,"DONE "+plateRound+" / "+questionCount(),86,878,18,bold,arcadeCyan);text(g,"CLICK TO FIRE",730,878,18,bold,arcadeYellow);arcadeButton(g,b,"게임 종료",790,965,152,42,false,()->exitQuiz());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,oldAA);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,oldTextAA);
    }
    void arcadeProgress(Graphics2D g,int x,int y,int w,double fraction,Color accent){g.setColor(new Color(31,42,70));g.fillRect(x,y,w,10);g.setColor(accent);g.fillRect(x,y,(int)Math.round(w*Math.max(0,Math.min(1,fraction))),10);g.setColor(Color.WHITE);g.drawRect(x,y,w,10);}
    void arcadeButton(Graphics2D g,boolean building,String label,int x,int y,int w,int h,boolean primary,Runnable action){Color fill=primary?new Color(30,190,210):new Color(18,28,56),edge=primary?new Color(255,224,91):new Color(61,240,255);g.setColor(fill);g.fillRect(x,y,w,h);g.setColor(edge);g.setStroke(new BasicStroke(2));g.drawRect(x,y,w,h);g.setColor(new Color(255,255,255,120));g.drawLine(x+5,y+5,x+w-6,y+5);g.setFont(bold.deriveFont(17f));center(g,label,x,y+11,w,17,bold,primary?Color.BLACK:Color.WHITE);if(building)hit(label,x,y,w,h,true,action);}
    void drawFlyingPlate(Graphics2D g,FlyingPlate p,String fact,Color edge,Color highlight){
        if(p.x<140||p.x>860)return;AffineTransform transform=g.getTransform();double tilt=plateVerticalScale(p);g.translate(p.x,p.y);g.scale(1,tilt);g.setColor(new Color(0,0,0,150));g.fillRect(-78,-16,156,40);g.setColor(new Color(226,238,245));g.fillRect(-80,-24,160,44);g.setColor(new Color(150,190,208));g.fillRect(-64,-35,128,11);g.setColor(edge);g.drawRect(-80,-24,160,44);g.setColor(highlight);g.fillRect(-48,-16,96,5);plateLabel(g,fact);g.setTransform(transform);
    }
    void plateLabel(Graphics2D g,String value){Font font=displayFont(value,bold,21);g.setFont(font);double natural=g.getFontMetrics().stringWidth(value);int size=(int)Math.max(12,Math.min(21,21*136/Math.max(136,natural)));center(g,value,-80,-10,160,size,bold,INK);}
    void drawShotEffects(Graphics2D g){
        double now=System.currentTimeMillis();for(ShotEffect shot:shotEffects){float alpha=(float)Math.max(0,Math.min(1,(shot.expires-now)/150.0));Color beam=shot.hit?(shot.correct?new Color(255,244,180,Math.round(alpha*220)):new Color(255,82,124,Math.round(alpha*220))):new Color(255,244,180,Math.round(alpha*210));g.setColor(beam);g.setStroke(new BasicStroke(4));g.drawLine(500,792,(int)Math.round(shot.x),(int)Math.round(shot.y));g.setColor(beam);g.fillRect((int)Math.round(shot.x-6),(int)Math.round(shot.y-6),12,12);g.fillRect((int)Math.round(shot.x-16),(int)Math.round(shot.y-2),32,4);g.fillRect((int)Math.round(shot.x-2),(int)Math.round(shot.y-16),4,32);}
    }
    void drawPlateOutcome(Graphics2D g,boolean b,Color cyan,Color pink){
        if(!pendingPlateOutcome)return;g.setColor(new Color(2,5,15,185));g.fillRect(58,175,884,665);Color accent=outcomeCorrect?cyan:pink;g.setColor(accent);g.fillRect(250,405,500,238);g.setColor(new Color(7,12,28));g.fillRect(258,413,484,222);g.setColor(accent);g.fillRect(282,448,54,54);g.setColor(Color.BLACK);g.fillRect(294,460,30,30);g.setColor(Color.WHITE);g.setStroke(new BasicStroke(4));if(outcomeCorrect){g.drawLine(291,475,301,485);g.drawLine(301,485,322,462);}else{g.drawLine(292,458,326,492);g.drawLine(326,458,292,492);}text(g,outcomeCorrect?"CORRECT!":"WRONG!",366,454,35,bold,accent);text(g,outcomeCorrect?"정답 접시 명중":"오답으로 기록됨",366,500,20,regular,Color.WHITE);if(!outcomeCorrect)text(g,"정답: "+currentQuestion().options()[currentQuestion().answer()],366,538,18,bold,Color.WHITE);String next=plateRound>=questionCount()?"결과 보기":"다음 문제";arcadeButton(g,b,next,366,575,300,42,true,this::resolvePlateOutcome);
    }
    void drawAimAndBlaster(Graphics2D g,Color cyan,Color yellow){
        double gunX=500,gunY=792,angle=Math.atan2(aimY-gunY,aimX-gunX);AffineTransform transform=g.getTransform();g.translate(gunX,gunY);g.rotate(angle);g.setColor(new Color(4,8,18));g.fillRect(-18,-12,70,24);g.setColor(cyan);g.fillRect(18,-7,36,14);g.setColor(yellow);g.fillRect(8,-4,10,8);g.setTransform(transform);
        int x=(int)Math.round(aimX),y=(int)Math.round(aimY);g.setColor(yellow);g.setStroke(new BasicStroke(3));g.drawRect(x-22,y-22,44,44);g.drawLine(x-38,y,x-10,y);g.drawLine(x+10,y,x+38,y);g.drawLine(x,y-38,x,y-10);g.drawLine(x,y+10,x,y+38);g.setColor(cyan);g.fillRect(x-4,y-4,8,8);
    }
    void feedback(Graphics2D g,boolean b){
        boolean correct=screen==Screen.CORRECT;int answer=answerIndex();photo(g,"feedback-campus",0,40,510,1040,false);fade(g,0,40,510,1040,true);text(g,(questionIndex+1)+" / "+questionCount(),44,67,25,bold,Color.BLACK);text(g,partLabel(),673,69,20,bold,Color.BLACK);checkIcon(g,396,207,64,correct);text(g,correct?"정답입니다!":"아쉬워요!",480,204,48,black,Color.BLACK);
        int answerY=correct?300:482;if(!correct){text(g,"내가 선택한 답변",396,299,18,regular,RED);round(g,396,335,548,83,5,new Color(253,241,240),RED);text(g,""+(char)('A'+selected)+"   "+options()[selected],424,357,27,bold,INK);text(g,"정답",396,447,18,bold,INK);}
        round(g,396,answerY,548,90,5,BLUE,null);text(g,""+(char)('A'+answer)+"   "+options()[answer],420,answerY+27,27,bold,Color.BLACK);line(g,396,answerY+118,548,LINE);text(g,"⊙ 해설",396,answerY+146,23,regular,Color.BLACK);paragraph(g,explanation(),396,answerY+188,548,24,regular,INK);pill(g,b,"다음 문제",396,954,548,true,true,true,this::nextAfterFeedback);
    }
    void exitModal(Graphics2D g,boolean b){
        g.setColor(new Color(0,0,0,102));g.fillRect(0,40,1000,1040);round(g,170,308,660,478,10,Color.WHITE,null);arrow(g,235,371,true,INK);text(g,"×",762,343,29,regular,INK);if(b)hit("종료 확인 닫기",748,340,50,50,true,()->show(beforeExit));text(g,part==1?"게임을 그만둘까요?":"퀴즈를 그만 풀까요?",216,413,37,black,INK);text(g,(part==1?"지금 종료하면 진행 중인 접시 기록은 저장되지 않아요.":"지금 종료하면 진행 중인 Part 0"+(part+1)+"의 답변은 저장되지 않아요.")+"\n파트 선택 화면으로 돌아갑니다.",216,490,23,regular,MUTED);round(g,216,585,568,62,6,PALE,null);text(g,"PART 0"+(part+1),236,603,19,bold,INK);text(g,part==1?plateRound+" / "+questionCount()+" 접시 진행 중":(questionIndex+1)+" / "+questionCount()+" 문항 진행 중",628,603,18,regular,INK);pill(g,b,"종료하기",216,675,276,false,true,false,()->{selected=-1;show(Screen.PARTS);});pill(g,b,"계속 풀기",508,675,276,true,true,false,()->show(beforeExit));
    }
    void partDone(Graphics2D g,boolean b){
        checkIcon(g,464,90,72,true);center(g,"PART 0"+(part+1)+" · "+PARTS[part],0,191,1000,21,bold,INK);center(g,new String[]{"첫 번째 파트 완료!","두 번째 파트 완료!","세 번째 파트 완료!"}[part],0,230,1000,49,black,Color.BLACK);center(g,part==2?"사진 속 건물을 잘 맞혀 보셨나요?":part==1?"날아오는 접시를 모두 명중했어요.":"캠퍼스를 알아가는 첫걸음, 잘 마쳤어요.",0,305,1000,23,regular,MUTED);
        int count=questionCount();round(g,100,367,800,298,10,Color.WHITE,LINE);text(g,"파트 점수",132,394,23,regular,Color.BLACK);text(g,""+partScore,132,421,78,black,Color.BLACK);text(g,"/ "+(count*10),250,472,30,regular,Color.BLACK);text(g,part==1?"명중":"정답",508,444,22,regular,Color.BLACK);text(g,(partScore/10)+" / "+count,508,482,36,regular,Color.BLACK);text(g,part==1?"플레이 완료":"풀이 완료",790,444,22,regular,Color.BLACK);text(g,count+" / "+count,790,482,36,regular,Color.BLACK);line(g,132,547,736,LINE);text(g,"전체 진행 상황",132,575,21,regular,INK);text(g,(part+1)+" / 3 파트 완료",750,575,20,bold,INK);for(int i=0;i<3;i++)round(g,132+i*248,625,239,8,4,i<=part?INK:LINE,null);
        round(g,100,693,800,116,6,PALE,null);text(g,part<2?"0"+(part+2):"03",124,713,52,bold,Color.BLACK);text(g,part<2?PARTS[part+1]:"거리뷰로 정답 장소를 찾아보세요!",220,717,24,bold,INK);text(g,part<2?"다음은 "+TAGLINES[part+1]:"정답 사진을 확인하고 캠퍼스를 탐색해요.",220,754,21,regular,MUTED);arrow(g,864,751,true,INK);pill(g,b,part<2?"다음 파트 시작하기":"거리뷰 게임 시작하기",100,837,800,true,true,true,this::nextPart);pill(g,b,"파트 선택으로",100,917,800,false,true,false,()->show(Screen.PARTS));brand(g,1010);
    }
    String streetFile(int n){return "street-"+n+".jpg";}
    int streetDestination(int position,String direction){
        int[][] links={{2,9,0,0},{3,8,0,0},{5,4,0,0},{14,8,0,0},{0,6,0,0},{7,14,0,0},{0,0,6,0},{12,11,0,0},{0,0,0,10},{8,11,0,0},{10,12,0,0},{13,14,0,0},{4,3,0,0}};
        int col=switch(direction){case "왼쪽"->0;case "오른쪽"->1;case "뒤쪽"->2;case "정면"->3;default->-1;};
        if(position<1||position>links.length||col<0)return 0;
        int dest=links[position-1][col];return dest==14?14:dest;
    }
    void moveStreet(String direction){int dest=streetDestination(streetPosition,direction);if(dest==0)return;if(dest==14){show(Screen.STREET_FOUND);return;}streetPosition=dest;show(Screen.STREET_GAME);}
    void streetArrow(Graphics2D g,boolean b,String direction,int cx,int cy){
        if(streetDestination(streetPosition,direction)==0)return;
        int dx=0,dy=0;switch(direction){case "왼쪽"->dx=-1;case "오른쪽"->dx=1;case "정면"->dy=-1;case "뒤쪽"->dy=1;}
        int tipX=cx+dx*31,tipY=cy+dy*31,baseX=cx-dx*5,baseY=cy-dy*5,px=-dy,py=dx;
        g.setStroke(new BasicStroke(19,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.setColor(new Color(0,0,0,115));g.drawLine(cx-dx*28+3,cy-dy*28+4,tipX+3,tipY+4);g.drawLine(tipX+3,tipY+4,baseX+px*23+3,baseY+py*23+4);g.drawLine(tipX+3,tipY+4,baseX-px*23+3,baseY-py*23+4);
        g.setStroke(new BasicStroke(19,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.setColor(Color.WHITE);g.drawLine(cx-dx*28,cy-dy*28,tipX,tipY);g.drawLine(tipX,tipY,baseX+px*23,baseY+py*23);g.drawLine(tipX,tipY,baseX-px*23,baseY-py*23);
        g.setStroke(new BasicStroke(13,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.setColor(new Color(25,126,245));g.drawLine(cx-dx*28,cy-dy*28,tipX,tipY);g.drawLine(tipX,tipY,baseX+px*23,baseY+py*23);g.drawLine(tipX,tipY,baseX-px*23,baseY-py*23);
        int size=116;if(b)hit(direction+" 화살표",cx-size/2,cy-size/2,size,size,true,()->moveStreet(direction));
    }
    void streetGame(Graphics2D g,boolean b){
        text(g,"사진에 보이는 복지관을 찾아가시오.",58,75,34,black,INK);text(g,"현재 위치: "+streetPosition+"번 사진 · 사진 속 화살표를 눌러 이동하세요.",58,119,20,regular,MUTED);
        photoFit(g,streetFile(streetPosition),58,160,884,540);round(g,72,174,132,44,8,new Color(0,0,0,180),null);center(g,streetPosition+"번",72,184,132,20,bold,Color.WHITE);
        streetArrow(g,b,"왼쪽",220,600);streetArrow(g,b,"정면",500,530);streetArrow(g,b,"뒤쪽",500,650);streetArrow(g,b,"오른쪽",780,600);
        round(g,58,736,884,180,8,PALE,null);text(g,"찾을 장소",80,755,19,bold,INK);photoFit(g,"street-answer.jpg",760,746,160,94);text(g,"사진에 보이는 복지관을 찾아가시오.",80,795,22,bold,INK);text(g,"사진 속 화살표를 눌러 연결된 장소로 이동하세요.",80,836,18,regular,MUTED);pill(g,b,"파트 선택으로",58,938,884,false,true,false,()->show(Screen.PARTS));
    }
    void streetFound(Graphics2D g,boolean b){
        text(g,"장소를 찾았어요!",58,94,44,black,INK);text(g,"거리뷰에서 정답 위치에 도착했습니다.",58,157,23,regular,MUTED);photoFit(g,"street-answer.jpg",58,220,884,590);round(g,58,834,884,92,8,PALE,null);text(g,"정답 장소에 도착했어요.",84,853,27,bold,INK);text(g,"세 번째 파트와 거리뷰 게임을 모두 완료했습니다.",84,894,20,regular,MUTED);pill(g,b,"최종 결과 확인하기",58,958,884,true,true,true,this::nextPart);
    }
    void finalResult(Graphics2D g,boolean b){
        int score=Arrays.stream(partScores).sum(),maximum=totalQuestionCount()*10,correct=score/10;double rate=score/(double)maximum;String percent=Math.round(rate*100)+"%";int wrong=totalQuestionCount()-correct;
        photo(g,"result-campus",630,40,370,1040,true);fade(g,630,40,290,1040,false);text(g,"☰",40,70,23,regular,INK);if(b)hit("파트 선택 메뉴",30,60,50,50,true,()->show(Screen.PARTS));text(g,"퀴즈 완료!",458,129,29,bold,INK);photo(g,"celebration",421,134,29,29,false);center(g,"최종 결과",0,170,1000,64,black,Color.BLACK);
        round(g,155,278,690,285,10,Color.WHITE,LINE);text(g,"총점",197,308,23,regular,Color.BLACK);text(g,""+score,197,351,108,black,Color.BLACK);text(g,"/ "+maximum,399,422,40,regular,Color.BLACK);text(g,"정답률 "+percent,399,477,26,regular,Color.BLACK);progress(g,197,524,606,rate);photo(g,"nice-work",740,235,170,145,false);
        String[] vals={correct+" / "+totalQuestionCount(),"04:32",rate==1.0?"KGU 마스터":"계속 도전"},labels={"정답","소요 시간","결과"};for(int i=0;i<3;i++){int cx=265+i*234;g.setColor(Color.WHITE);g.fillOval(cx-39,590,78,78);g.setColor(INK);g.setStroke(new BasicStroke(1.5f));g.drawOval(cx-39,590,78,78);photo(g,new String[]{"trophy","clock","star"}[i],cx-24,605,48,47,false);center(g,labels[i],cx-100,681,200,23,regular,INK);center(g,vals[i],cx-110,718,220,26,regular,Color.BLACK);}
        center(g,rate==1.0?"이제 당신은 KGU 마스터!":"퀴즈를 모두 마쳤어요!",0,777,1000,27,bold,INK);center(g,"새롭게 알게 된 정보와 함께 캠퍼스 생활을 시작해 보세요.",0,826,1000,21,regular,MUTED);center(g,wrong==0?"모든 문제를 맞혔어요!":"틀린 "+wrong+"문제도 다시 확인해 보세요.",0,856,1000,21,regular,MUTED);pill(g,b,wrong==0?"최종 결과 확인하기":"오답 "+wrong+"문제 복습하기",155,891,690,true,true,true,()->show(Screen.REVIEW));pill(g,b,"다시 도전하기",38,975,372,false,true,false,()->show(Screen.PARTS));pill(g,b,"처음으로",590,975,372,true,true,false,this::reset);
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
    void exportPreviews()throws IOException{File dir=new File("previews/pdf-design");dir.mkdirs();for(Screen s:Screen.values()){part=s==Screen.PLATE_GAME?1:(s==Screen.STREET_GAME||s==Screen.STREET_FOUND?2:0);plateRound=s==Screen.PLATE_GAME?2:0;partScore=s==Screen.PLATE_GAME?plateRound*10:0;streetPosition=1;reviewIndex=0;selected=s==Screen.QUESTION?-1:s==Screen.INCORRECT?1:0;flyingPlates.clear();pendingPlateOutcome=false;outcomeCorrect=false;if(s==Screen.PLATE_GAME)spawnPreviewPlates();screen=s;rebuild();root.setSize(WIDTH,HEIGHT);root.doLayout();BufferedImage out=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);Graphics2D g=out.createGraphics();root.printAll(g);g.dispose();String file=switch(s){case WELCOME->"01-welcome";case GUIDE->"02-guide";case PARTS->"03-parts";case INTRO->"04-intro";case QUESTION->"05-question";case SELECTED->"06-selected";case PLATE_GAME->"plate-game";case CORRECT->"07-correct";case INCORRECT->"08-incorrect";case EXIT->"09-exit";case PART_DONE->"10-part_done";case STREET_GAME->"street-game";case STREET_FOUND->"street-found";case FINAL->"11-final";case REVIEW->"12-review";case REVIEW_DETAIL->"13-review_detail";};ImageIO.write(out,"png",new File(dir,file+".png"));}System.out.println("Rendered "+Screen.values().length+" Swing screens to previews/pdf-design/");}
    public static void main(String[] args)throws Exception{boolean preview=Arrays.asList(args).contains("--preview");if(preview)System.setProperty("java.awt.headless","true");loadFonts();SwingUtilities.invokeAndWait(()->{CampusQuiz app=new CampusQuiz();if(preview){try{app.exportPreviews();}catch(IOException e){throw new RuntimeException(e);}return;}app.frame=new JFrame("KGU QUIZ · UI Demo");app.frame.setUndecorated(true);app.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);app.frame.setContentPane(app.root);app.frame.pack();app.frame.setMinimumSize(new Dimension(600,648));app.frame.setLocationRelativeTo(null);app.frame.setVisible(true);});}
}
