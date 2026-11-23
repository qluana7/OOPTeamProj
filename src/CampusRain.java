import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.im.InputContext;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.AttributedCharacterIterator;
import java.text.Normalizer;
import java.util.*;
import java.util.List;

/** KGU CAMPUS RAIN. Java 17 Swing / 외부 라이브러리 불필요. */
public class CampusRain {
    static final double DESIGN_WIDTH=1280, DESIGN_HEIGHT=800, FIELD_TOP=226, DEADLINE=588, TAG_HEIGHT=44;
    static final Color INK=new Color(12,16,21), RED=new Color(226,0,36), GRAY_HEART=new Color(145,153,165);
    static final Path HOME=Path.of(System.getProperty("rain.home",".")).toAbsolutePath().normalize();
    static String normalize(String value){return Normalizer.normalize(value,Normalizer.Form.NFKC).replaceAll("\\s+","").toLowerCase(Locale.ROOT);}
    record Building(String name,String description,List<String> aliases,String source){
        boolean matches(String value){String key=normalize(value);return normalize(name).equals(key)||aliases.stream().anyMatch(a->normalize(a).equals(key));}
    }
    static List<Building> loadBuildings()throws IOException {
        String override=System.getProperty("rain.data");Path file=override==null?HOME.resolve("assets/rain/buildings.txt"):Path.of(override);
        InputStream stream;
        if(Files.isRegularFile(file))stream=Files.newInputStream(file);
        else if(override!=null)throw new IOException("건물 파일을 찾을 수 없습니다: "+file);
        else {stream=CampusRain.class.getResourceAsStream("/assets/rain/buildings.txt");if(stream==null)stream=CampusRain.class.getResourceAsStream("/buildings.txt");if(stream==null)throw new IOException("assets/rain/buildings.txt가 없습니다.");}
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))){return parseBuildings(reader);}
    }
    static List<Building> parseBuildings(BufferedReader reader)throws IOException {
        List<Building> buildings=new ArrayList<>();Map<String,String> keys=new HashMap<>();String line;int number=0;
        while((line=reader.readLine())!=null){number++;line=line.replace("\uFEFF","").strip();if(line.isEmpty()||line.startsWith("#"))continue;
            String[] parts=line.split("\\|",-1);if(parts.length!=4||parts[0].isBlank()||parts[1].isBlank())throw new IOException(number+"행: 이름|설명|별칭|출처 형식을 확인하세요.");
            String name=parts[0].strip();List<String> aliases=parts[2].isBlank()?List.of():Arrays.stream(parts[2].split(";")).map(String::strip).filter(a->!a.isEmpty()).toList();
            List<String> names=new ArrayList<>(aliases);names.add(name);
            for(String text:names){String key=normalize(text);String previous=keys.putIfAbsent(key,name);if(previous!=null&&!previous.equals(name))throw new IOException(number+"행: 다른 건물과 입력 이름이 중복됩니다: "+text);}
            if(buildings.stream().anyMatch(b->normalize(b.name()).equals(normalize(name))))throw new IOException(number+"행: 중복 건물 "+name);
            buildings.add(new Building(name,parts[1].strip(),aliases,parts[3].strip()));
        }
        if(buildings.isEmpty())throw new IOException("등록된 건물이 없습니다.");return List.copyOf(buildings);
    }
    enum State {READY,PLAYING,PAUSED,GAME_OVER}
    enum Submission {CORRECT,WRONG,EMPTY,INACTIVE}
    static class Falling {
        final long id;final Building building;final double horizontal;double progress;
        Falling(long id,Building building,double horizontal,double progress){this.id=id;this.building=building;this.horizontal=horizontal;this.progress=progress;}
    }
    /** 게임 규칙은 화면과 분리하여 시간·입력·생명 처리를 검증합니다. EDT에서만 변경합니다. */
    static class Game {
        final List<Building> catalog;final Random random;final List<Falling> falling=new ArrayList<>();
        State state=State.READY;int lives=5,hits=0;double elapsed=0,spawnIn=.75,errorUntil=-1;long nextId=0;Building lastCorrect;
        Game(List<Building> catalog,Random random){if(catalog.isEmpty())throw new IllegalArgumentException("건물이 필요합니다.");this.catalog=List.copyOf(catalog);this.random=random;}
        void start(){state=State.PLAYING;lives=5;hits=0;elapsed=0;spawnIn=.75;errorUntil=-1;nextId=0;lastCorrect=null;falling.clear();while(falling.size()<maxOnScreen())spawn();}
        int maxOnScreen(){return 3+Math.min(2,(int)((elapsed+1e-8)/30));}
        void spawn(){
            if(state!=State.PLAYING||falling.size()>=maxOnScreen())return;
            List<Building> available=catalog.stream().filter(b->falling.stream().noneMatch(f->f.building==b)).toList();
            Building b=(available.isEmpty()?catalog:available).get(random.nextInt((available.isEmpty()?catalog:available).size()));
            double x=random.nextDouble(),best=x,bestGap=-1;
            for(int attempt=0;attempt<60;attempt++){double candidate=random.nextDouble(),gap=1;for(Falling f:falling)if(f.progress<.20)gap=Math.min(gap,Math.abs(candidate-f.horizontal));if(gap>bestGap){bestGap=gap;best=candidate;}if(gap>.22){best=candidate;break;}}
            falling.add(new Falling(nextId++,b,best,0));
        }
        double speed(){return Math.min(2.5,Math.pow(1.1,Math.floor((elapsed+1e-8)/20.0)));}
        double errorAlpha(){if(state!=State.PLAYING)return 0;double remaining=errorUntil-elapsed;return remaining<=0?0:Math.min(1,remaining/.3);}
        void pause(){if(state==State.PLAYING)state=State.PAUSED;}
        void resume(){if(state==State.PAUSED)state=State.PLAYING;}
        void advance(double seconds){
            if(!Double.isFinite(seconds)||seconds<0)throw new IllegalArgumentException("유효한 경과 시간이 필요합니다.");
            while(seconds>1e-9&&state==State.PLAYING){double dt=Math.min(.05,seconds);seconds-=dt;elapsed+=dt;
                double movement=dt*speed()/12.0;Iterator<Falling> iterator=falling.iterator();while(iterator.hasNext()){
                    Falling f=iterator.next();f.progress+=movement;
                    if(f.progress>=1){iterator.remove();lives--;if(lives==0){state=State.GAME_OVER;errorUntil=-1;break;}}
                }
                if(state!=State.PLAYING)break;
                spawnIn-=dt;while(spawnIn<=0){spawn();spawnIn+=.75;}
            }
        }
        Submission submit(String text){
            if(state!=State.PLAYING)return Submission.INACTIVE;
            if(normalize(text).isEmpty())return Submission.EMPTY;
            Falling matched=falling.stream().filter(f->f.building.matches(text)).max(Comparator.comparingDouble((Falling f)->f.progress).thenComparingLong(f->-f.id)).orElse(null);
            if(matched==null){errorUntil=elapsed+1.2;return Submission.WRONG;}
            falling.remove(matched);hits++;lastCorrect=matched.building;errorUntil=-1;return Submission.CORRECT;
        }
    }
    static BufferedImage background(){
        Path external=HOME.resolve("assets/rain/campus-background.png");
        try{if(Files.isRegularFile(external))return ImageIO.read(external.toFile());InputStream candidate=CampusRain.class.getResourceAsStream("/assets/rain/campus-background.png");if(candidate==null)candidate=CampusRain.class.getResourceAsStream("/campus-background.png");if(candidate==null)return null;final InputStream stream=candidate;try(stream){return ImageIO.read(stream);}}catch(IOException e){return null;}
    }
    static void quality(Graphics2D g){g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);}
    static final Map<Integer,Font> FONTS=new HashMap<>();
    static Font font(int size,boolean bold){return FONTS.computeIfAbsent(size*2+(bold?1:0),key->new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size));}
    static String time(double elapsed){int seconds=(int)elapsed;return String.format("%02d:%02d",seconds/60,seconds%60);}
    static void drawText(Graphics2D g,String text,double x,double baseline,int size,boolean bold,Color color){g.setColor(color);g.setFont(font(size,bold));g.drawString(text,(float)x,(float)baseline);}
    static List<String> wrap(String text,FontMetrics fm,int width){
        List<String> lines=new ArrayList<>();for(String paragraph:text.split("\n",-1)){StringBuilder line=new StringBuilder();for(int cp:paragraph.codePoints().toArray()){String ch=new String(Character.toChars(cp));if(line.length()>0&&fm.stringWidth(line+ch)>width){lines.add(line.toString());line.setLength(0);}line.append(ch);}lines.add(line.toString());}return lines;
    }
    static void paragraph(Graphics2D g,String text,int x,int baseline,int width,int size,int maxLines,Color color){
        List<String> lines;do{g.setFont(font(size,false));lines=wrap(text,g.getFontMetrics(),width);if(lines.size()<=maxLines||size<=11)break;size--;}while(true);
        g.setColor(color);int step=g.getFontMetrics().getHeight()+3;for(int i=0;i<Math.min(maxLines,lines.size());i++)g.drawString(lines.get(i),x,baseline+i*step);
    }
    static class PlaceholderField extends JTextField {
        PlaceholderField(){setOpaque(false);setBorder(new EmptyBorder(0,18,0,12));enableInputMethods(true);}
        @Override protected void paintComponent(Graphics graphics){super.paintComponent(graphics);if(getText().isEmpty()&&!hasFocus()){Graphics2D g=(Graphics2D)graphics.create();g.setFont(getFont());g.setColor(new Color(130,135,143));FontMetrics fm=g.getFontMetrics();g.drawString("떨어지는 건물 이름을 입력하세요",getInsets().left,(getHeight()-fm.getHeight())/2+fm.getAscent());g.dispose();}}
    }
    static class DarkButton extends JButton {
        DarkButton(String title){super(title);setUI(new javax.swing.plaf.basic.BasicButtonUI());setContentAreaFilled(false);setOpaque(false);setFocusPainted(false);setBorder(new EmptyBorder(4,12,4,12));setForeground(Color.WHITE);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
        @Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();quality(g);g.setColor(!isEnabled()?new Color(130,138,149):getModel().isArmed()?new Color(50,57,67):INK);g.fillRoundRect(0,0,getWidth(),getHeight(),14,14);if(hasFocus()){g.setColor(new Color(70,133,220));g.setStroke(new BasicStroke(2));g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,14,14);}g.dispose();super.paintComponent(graphics);}
    }
    static class GameView extends JPanel {
        final Game game;final BufferedImage image;final PlaceholderField input=new PlaceholderField();final DarkButton submit=new DarkButton("입력 ↵");
        final Overlay overlay;final javax.swing.Timer timer;final boolean animate;long lastNanos;boolean composing,submissionQueued;
        State syncedState;JRootPane syncedRoot;
        BufferedImage sceneCache,headerCache,infoCache,footerCache;int cacheWidth=-1,cacheHeight=-1,cachedLives=-1,cachedSeconds=-1,cachedHits=-1;double cachedSpeed=-1;
        Building cachedInfo;boolean infoBuilt;long sceneBuilds,headerBuilds,infoBuilds,footerBuilds,spriteBuilds;
        final FontMetrics wordMetrics=getFontMetrics(font(27,true));
        record SpriteKey(Building building,boolean urgent){} record Sprite(BufferedImage image){}
        final Map<SpriteKey,Sprite> sprites=new HashMap<>();
        GameView(Game game,boolean animate){
            this.game=game;this.animate=animate;image=background();setLayout(null);setBackground(new Color(236,245,255));setPreferredSize(new Dimension(1280,800));
            add(input);add(submit);overlay=new Overlay(this);add(overlay);setComponentZOrder(overlay,0);
            input.addActionListener(e->submitAfterComposition());submit.addActionListener(e->submitAfterComposition());
            input.addInputMethodListener(new InputMethodListener(){public void inputMethodTextChanged(InputMethodEvent e){AttributedCharacterIterator it=e.getText();int total=it==null?0:it.getEndIndex()-it.getBeginIndex();composing=total>e.getCommittedCharacterCount();}public void caretPositionChanged(InputMethodEvent e){}});
            lastNanos=System.nanoTime();timer=new javax.swing.Timer(33,e->tick());timer.setCoalesce(true);sync();
        }
        double scale(){return Math.min(getWidth()/DESIGN_WIDTH,getHeight()/DESIGN_HEIGHT);}
        double offsetX(){return (getWidth()-DESIGN_WIDTH*scale())/2;}
        double offsetY(){return (getHeight()-DESIGN_HEIGHT*scale())/2;}
        void place(Component c,int x,int y,int w,int h){double s=scale();c.setBounds((int)Math.round(offsetX()+x*s),(int)Math.round(offsetY()+y*s),(int)Math.round(w*s),(int)Math.round(h*s));}
        @Override public void doLayout(){place(input,316,660,532,50);place(submit,848,660,116,50);input.setFont(font(Math.max(14,(int)(22*scale())),false));submit.setFont(font(Math.max(13,(int)(22*scale())),true));overlay.setBounds(0,0,getWidth(),getHeight());overlay.doLayout();}
        void submitAfterComposition(){
            if(game.state!=State.PLAYING||submissionQueued)return;
            if(composing){submissionQueued=true;InputContext context=input.getInputContext();if(context!=null)context.endComposition();SwingUtilities.invokeLater(()->{submissionQueued=false;composing=false;submitInput();});}else submitInput();
        }
        void submitInput(){Submission result=game.submit(input.getText());if(result==Submission.CORRECT)input.setText("");else if(result==Submission.WRONG)input.selectAll();input.requestFocusInWindow();sync();repaint();}
        void startOrResume(){if(game.state==State.PAUSED)game.resume();else{game.start();input.setText("");}lastNanos=System.nanoTime();sync();input.requestFocusInWindow();repaint();}
        void pause(){game.pause();sync();repaint();}
        void sync(){
            boolean changed=syncedState!=game.state,playing=game.state==State.PLAYING;JRootPane root=SwingUtilities.getRootPane(this);
            if(changed){syncedState=game.state;input.setEnabled(playing);submit.setEnabled(playing);overlay.setVisible(!playing);overlay.action.setText(game.state==State.READY?"게임 시작하기":game.state==State.PAUSED?"계속하기":"다시 도전하기");if(playing&&animate){lastNanos=System.nanoTime();timer.start();}else timer.stop();repaint();}
            if(root!=null&&(changed||syncedRoot!=root)){root.setDefaultButton(playing?submit:overlay.action);syncedRoot=root;}
        }
        void dispose(){timer.stop();}
        void repaintRegion(double x,double y,double w,double h){double s=scale();int left=(int)Math.floor(offsetX()+x*s)-2,top=(int)Math.floor(offsetY()+y*s)-2;repaint(left,top,(int)Math.ceil(w*s)+4,(int)Math.ceil(h*s)+4);}
        void tick(){
            long now=System.nanoTime();double dt=Math.min(.10,Math.max(0,(now-lastNanos)/1_000_000_000.0));lastNanos=now;
            int oldLives=game.lives,oldSeconds=(int)game.elapsed;double oldSpeed=game.speed(),oldError=game.errorAlpha();game.advance(dt);
            if(game.state!=syncedState){sync();return;}
            repaintRegion(0,208,DESIGN_WIDTH,DEADLINE-208+3);
            if(oldLives!=game.lives)repaintRegion(0,0,DESIGN_WIDTH,96);
            if(oldSeconds!=(int)game.elapsed||oldSpeed!=game.speed())repaintRegion(0,754,DESIGN_WIDTH,46);
            if(oldError>0||game.errorAlpha()>0)repaintRegion(0,604,DESIGN_WIDTH,44);
        }
        BufferedImage layer(int y,int height,java.util.function.Consumer<Graphics2D> paint){
            double s=scale();BufferedImage result=new BufferedImage(Math.max(1,(int)Math.ceil(DESIGN_WIDTH*s)),Math.max(1,(int)Math.ceil(height*s)),BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g=result.createGraphics();quality(g);g.scale(s,s);g.translate(0,-y);paint.accept(g);g.dispose();return result;
        }
        void ensureCaches(){
            if(getWidth()!=cacheWidth||getHeight()!=cacheHeight){
                cacheWidth=getWidth();cacheHeight=getHeight();sprites.clear();cachedLives=-1;infoBuilt=false;cachedSeconds=-1;
                sceneCache=new BufferedImage(Math.max(1,cacheWidth),Math.max(1,cacheHeight),BufferedImage.TYPE_INT_RGB);Graphics2D bg=sceneCache.createGraphics();quality(bg);bg.setColor(getBackground());bg.fillRect(0,0,cacheWidth,cacheHeight);bg.translate(offsetX(),offsetY());bg.scale(scale(),scale());
                bg.setColor(new Color(235,245,255));bg.fillRect(0,0,(int)DESIGN_WIDTH,(int)DESIGN_HEIGHT);if(image!=null)bg.drawImage(image,0,0,(int)DESIGN_WIDTH,(int)DESIGN_HEIGHT,null);paintDeadline(bg);paintInputStatic(bg);bg.dispose();sceneBuilds++;
            }
            if(cachedLives!=game.lives){headerCache=layer(0,96,this::paintHeader);cachedLives=game.lives;headerBuilds++;}
            if(!infoBuilt||cachedInfo!=game.lastCorrect){infoCache=layer(96,112,this::paintInfo);cachedInfo=game.lastCorrect;infoBuilt=true;infoBuilds++;}
            if(cachedSeconds!=(int)game.elapsed||cachedHits!=game.hits||cachedSpeed!=game.speed()){
                footerCache=layer(754,46,this::paintFooter);cachedSeconds=(int)game.elapsed;cachedHits=game.hits;cachedSpeed=game.speed();footerBuilds++;
            }
        }
        void blit(Graphics2D g,BufferedImage image,int logicalY){g.drawImage(image,(int)Math.round(offsetX()),(int)Math.round(offsetY()+logicalY*scale()),null);}
        @Override protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);if(getWidth()<1||getHeight()<1)return;ensureCaches();Graphics2D g=(Graphics2D)graphics.create();g.drawImage(sceneCache,0,0,null);blit(g,headerCache,0);blit(g,infoCache,96);blit(g,footerCache,754);paintSprites(g);
            if(game.errorAlpha()>0){g.translate(offsetX(),offsetY());g.scale(scale(),scale());quality(g);paintError(g);}g.dispose();
        }
        Sprite sprite(Building building,boolean urgent){
            SpriteKey key=new SpriteKey(building,urgent);Sprite cached=sprites.get(key);if(cached!=null)return cached;
            double width=wordMetrics.stringWidth(building.name())+32,s=scale();BufferedImage image=new BufferedImage(Math.max(1,(int)Math.ceil(width*s)+2),Math.max(1,(int)Math.ceil((TAG_HEIGHT+38)*s)+2),BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g=image.createGraphics();quality(g);g.scale(s,s);g.setColor(urgent?new Color(239,177,65,130):new Color(85,142,205,110));g.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            for(int i=0;i<3;i++){double xx=width/2+(i-1)*15;g.draw(new Line2D.Double(xx,11-(i%2)*8,xx,28));}
            g.setColor(urgent?new Color(255,232,177,235):new Color(255,255,255,220));g.fill(new RoundRectangle2D.Double(0,38,width,TAG_HEIGHT,17,17));g.setFont(font(27,true));g.setColor(INK);g.drawString(building.name(),16,38+(float)(TAG_HEIGHT-wordMetrics.getHeight())/2+wordMetrics.getAscent());g.dispose();
            Sprite result=new Sprite(image);sprites.put(key,result);spriteBuilds++;return result;
        }
        void paintSprites(Graphics2D g){
            Shape clip=g.getClip();g.clip(new Rectangle2D.Double(offsetX(),offsetY()+208*scale(),DESIGN_WIDTH*scale(),(DEADLINE-208)*scale()));
            for(Falling falling:game.falling){Rectangle2D rect=wordBounds(falling,wordMetrics);Sprite sprite=sprite(falling.building,falling.progress>.78);g.drawImage(sprite.image(),(int)Math.round(offsetX()+rect.getX()*scale()),(int)Math.round(offsetY()+(rect.getY()-38)*scale()),null);}g.setClip(clip);
        }
        void paintHeader(Graphics2D g){
            g.setPaint(new GradientPaint(0,0,new Color(255,255,255,225),1280,96,new Color(223,237,255,175)));g.fillRect(0,0,1280,96);
            g.setPaint(new GradientPaint(28,15,new Color(57,89,239),28,76,new Color(16,100,127)));g.fillRect(28,15,7,61);
            drawText(g,"KGU CAMPUS RAIN",49,54,42,true,INK);drawText(g,"경기대 건물 타자연습",50,80,23,true,new Color(98,105,116));
            for(int i=0;i<5;i++)heart(g,994+i*51,15,43,37,i<game.lives?RED:GRAY_HEART);
            drawText(g,"놓침 "+(5-game.lives)+" / 5",1073,79,20,true,INK);
        }
        void heart(Graphics2D g,double x,double y,double w,double h,Color color){
            Path2D path=new Path2D.Double();path.moveTo(16,29);path.curveTo(12,26,-1,17,0,9);path.curveTo(0,-1,11,-3,16,5);path.curveTo(21,-3,32,-1,32,9);path.curveTo(33,17,20,26,16,29);path.closePath();AffineTransform transform=new AffineTransform();transform.translate(x,y);transform.scale(w/32,h/29);g.setColor(color);g.fill(transform.createTransformedShape(path));
        }
        void paintInfo(Graphics2D g){
            g.setColor(new Color(235,240,247,235));g.fillRect(0,96,1280,112);drawText(g,"방금 알아본 건물",32,126,18,false,INK);
            String name=game.lastCorrect==null?"건물을 맞혀보세요":game.lastCorrect.name();int size=game.lastCorrect==null?28:43;g.setFont(font(size,true));while(g.getFontMetrics().stringWidth(name)>258&&size>15)g.setFont(font(--size,true));g.setColor(INK);g.drawString(name,32,180);
            g.setColor(new Color(164,174,188));g.setStroke(new BasicStroke(1));g.drawLine(307,135,307,189);
            String desc=game.lastCorrect==null?"떨어지는 건물 이름을 입력하면 이름과 설명이 여기에 표시됩니다.":game.lastCorrect.description();paragraph(g,desc,345,153,890,22,2,INK);
        }
        Rectangle2D wordBounds(Falling f,FontMetrics metrics){double width=metrics.stringWidth(f.building.name())+32;double x=24+f.horizontal*(DESIGN_WIDTH-width-48);double y=FIELD_TOP+f.progress*(DEADLINE-FIELD_TOP-TAG_HEIGHT);return new Rectangle2D.Double(x,y,width,TAG_HEIGHT);}
        void paintDeadline(Graphics2D g){g.setColor(RED);g.setStroke(new BasicStroke(3,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,1,new float[]{13,8},0));g.draw(new Line2D.Double(0,DEADLINE,DESIGN_WIDTH,DEADLINE));String caption="이 선을 넘으면 생명 -1";g.setFont(font(18,true));drawText(g,caption,1250-g.getFontMetrics().stringWidth(caption),DEADLINE-14,18,true,RED);}
        void paintError(Graphics2D g){
            double alpha=game.errorAlpha();if(alpha>0){String message="틀린 이름입니다";g.setComposite(AlphaComposite.SrcOver.derive((float)alpha));g.setFont(font(27,true));g.setColor(RED);g.drawString(message,(float)((DESIGN_WIDTH-g.getFontMetrics().stringWidth(message))/2),635);g.setComposite(AlphaComposite.SrcOver);}
        }
        void paintInputStatic(Graphics2D g){
            g.setColor(Color.WHITE);g.fillRoundRect(315,659,650,52,13,13);g.setColor(new Color(83,94,110));g.setStroke(new BasicStroke(1.4f));g.drawRoundRect(315,659,650,52,13,13);
            String hint="Enter로 제출 · 오타는 생명이 줄지 않아요";g.setFont(font(16,false));g.setColor(new Color(83,94,110));g.drawString(hint,(float)((DESIGN_WIDTH-g.getFontMetrics().stringWidth(hint))/2),736);
        }
        void paintFooter(Graphics2D g){g.setColor(new Color(214,225,240,210));g.fillRect(0,754,1280,46);drawText(g,"맞힌 건물  "+game.hits,30,784,21,true,INK);String status="진행 시간  "+time(game.elapsed)+"   ·   낙하 속도  ×"+String.format(Locale.ROOT,"%.1f",game.speed());g.setFont(font(20,true));g.setColor(INK);g.drawString(status,1250-g.getFontMetrics().stringWidth(status),784);}
    }
    static class Overlay extends JPanel {
        final GameView view;final DarkButton action=new DarkButton("게임 시작하기"),close=new DarkButton("종료");
        Overlay(GameView view){this.view=view;setLayout(null);setOpaque(false);addMouseListener(new MouseAdapter(){});add(action);add(close);action.addActionListener(e->view.startOrResume());close.addActionListener(e->{view.dispose();Window window=SwingUtilities.getWindowAncestor(view);if(window!=null)window.dispose();});}
        void place(Component component,int x,int y,int w,int h){double s=view.scale();component.setBounds((int)(view.offsetX()+x*s),(int)(view.offsetY()+y*s),(int)(w*s),(int)(h*s));}
        @Override public void doLayout(){place(action,420,482,320,48);place(close,752,482,108,48);action.setFont(font(Math.max(14,(int)(20*view.scale())),true));close.setFont(action.getFont());}
        @Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();quality(g);g.setColor(new Color(239,246,255,170));g.fillRect(0,0,getWidth(),getHeight());g.translate(view.offsetX(),view.offsetY());g.scale(view.scale(),view.scale());g.setColor(new Color(255,255,255,245));g.fillRoundRect(360,267,560,292,24,24);g.setColor(new Color(195,210,229));g.setStroke(new BasicStroke(1));g.drawRoundRect(360,267,560,292,24,24);
            State state=view.game.state;String title=state==State.READY?"건물 이름이 비처럼 내려요":state==State.PAUSED?"잠시 멈췄어요":"게임 종료";g.setFont(font(30,true));g.setColor(INK);g.drawString(title,(float)((DESIGN_WIDTH-g.getFontMetrics().stringWidth(title))/2),325);
            String details=state==State.READY?"이름을 입력하고 Enter를 눌러 건물 설명을 알아보세요.\n붉은 선을 넘긴 이름 5개가 쌓이면 게임이 끝나요.\n오타는 생명을 줄이지 않아요.":state==State.PAUSED?"창을 벗어나 게임을 일시 정지했습니다.\n준비되면 계속하기를 눌러 주세요.":"맞힌 건물 "+view.game.hits+"개  ·  플레이 시간 "+time(view.game.elapsed)+"\n놓친 이름 5개로 하트가 모두 회색이 되었어요.\n다시 도전해서 더 많은 건물을 알아보세요.";
            int y=378;for(String line:details.split("\n")){g.setFont(font(17,false));g.setColor(new Color(80,92,108));g.drawString(line,(float)((DESIGN_WIDTH-g.getFontMetrics().stringWidth(line))/2),y);y+=29;}g.dispose();
        }
    }
    static JFrame rainFrame;
    /** Opens the rain game from the quiz without replacing the quiz window. */
    static void openWindow(){
        if(GraphicsEnvironment.isHeadless())return;
        Runnable open=()->{try{List<Building> buildings=loadBuildings();if(rainFrame!=null&&rainFrame.isDisplayable()){rainFrame.toFront();rainFrame.requestFocus();return;}
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            GameView view=new GameView(new Game(buildings,new Random()),true);JFrame frame=new JFrame("KGU CAMPUS RAIN · 경기대 건물 타자연습");rainFrame=frame;frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);frame.setContentPane(view);frame.pack();frame.setMinimumSize(new Dimension(960,640));frame.setLocationRelativeTo(null);frame.addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){view.dispose();rainFrame=null;}});frame.addWindowFocusListener(new WindowAdapter(){@Override public void windowLostFocus(WindowEvent e){view.pause();}});frame.setVisible(true);view.sync();view.overlay.action.requestFocusInWindow();
        }catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"실행 오류",JOptionPane.ERROR_MESSAGE);}};
        if(SwingUtilities.isEventDispatchThread())open.run();else SwingUtilities.invokeLater(open);
    }
    public static void main(String[] args){
        try{List<Building> buildings=loadBuildings();if(Arrays.asList(args).contains("--check")){System.out.println("건물 데이터 검증 완료: "+buildings.size()+"개");return;}
            if(GraphicsEnvironment.isHeadless()){System.err.println("GUI 환경에서 실행해 주세요. 데이터 검증은 --check를 사용하세요.");System.exit(1);}
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            SwingUtilities.invokeLater(()->{GameView view=new GameView(new Game(buildings,new Random()),true);JFrame frame=new JFrame("KGU CAMPUS RAIN · 경기대 건물 타자연습");frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);frame.setContentPane(view);frame.pack();frame.setMinimumSize(new Dimension(960,640));frame.setLocationRelativeTo(null);frame.addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){view.dispose();}});frame.addWindowFocusListener(new WindowAdapter(){@Override public void windowLostFocus(WindowEvent e){view.pause();}});frame.setVisible(true);view.sync();view.overlay.action.requestFocusInWindow();});
        }catch(Exception e){System.err.println("실행 오류: "+e.getMessage());if(!GraphicsEnvironment.isHeadless())JOptionPane.showMessageDialog(null,e.getMessage(),"실행 오류",JOptionPane.ERROR_MESSAGE);System.exit(1);}
    }
}
