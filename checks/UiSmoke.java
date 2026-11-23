import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/** Meaningful checks for the PDF design's navigation and scaled button geometry. */
public class UiSmoke {
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static JButton button(CampusQuiz app,String name){for(Component c:app.root.getComponents())if(c instanceof JButton b&&name.equals(b.getText()))return b;throw new AssertionError("Missing action: "+name);}
    static void click(CampusQuiz app,String name){JButton b=button(app,name);check(b.isEnabled(),"Disabled action: "+name);b.doClick();}
    static CampusQuiz.FlyingPlate correctPlate(CampusQuiz app){for(CampusQuiz.FlyingPlate p:app.flyingPlates)if(p.factIndex==app.answerIndex())return p;throw new AssertionError("Correct answer plate not spawned");}
    static void shootCorrect(CampusQuiz app){CampusQuiz.FlyingPlate p=correctPlate(app);app.fireAt(p.x,p.y);}
    public static void main(String[] args)throws Exception{
        System.setProperty("java.awt.headless","true");CampusQuiz.loadFonts();SwingUtilities.invokeAndWait(()->{
            CampusQuiz app=new CampusQuiz();int renders=0;
            for(CampusQuiz.Screen screen:CampusQuiz.Screen.values())for(Dimension size:new Dimension[]{new Dimension(600,648),new Dimension(800,864),new Dimension(1000,1080),new Dimension(1200,900)}){
                app.part=screen==CampusQuiz.Screen.PLATE_GAME?1:0;app.plateRound=screen==CampusQuiz.Screen.PLATE_GAME?2:0;app.selected=screen==CampusQuiz.Screen.QUESTION?-1:screen==CampusQuiz.Screen.INCORRECT?1:0;app.show(screen);app.root.setSize(size);app.root.doLayout();
                for(Component c:app.root.getComponents()){check(!(c instanceof JScrollPane),"Unexpected scrolling");check(c.getX()>=0&&c.getY()>=0&&c.getX()+c.getWidth()<=size.width+1&&c.getY()+c.getHeight()<=size.height+1,"Button outside viewport");}
                BufferedImage render=new BufferedImage(size.width,size.height,BufferedImage.TYPE_INT_RGB);Graphics2D g=render.createGraphics();app.root.printAll(g);g.dispose();renders++;
            }
            app.reset();click(app,"처음이라면? 이용 안내");check(app.screen==CampusQuiz.Screen.GUIDE,"Guide");click(app,"확인했어요 · 파트 선택하기");click(app,"KGU CAMPUS GUIDE");click(app,"Part 01 시작하기");check(!button(app,"다음 문제").isEnabled(),"Require selection");
            click(app,"답변 B: 제1복지관 지하 1층");click(app,"퀴즈 종료");check(app.screen==CampusQuiz.Screen.EXIT,"Exit modal");check(app.hotspots.stream().noneMatch(h->h.name().startsWith("답변")),"Modal blocks background actions");click(app,"계속 풀기");check(app.selected==1,"Preserve answer on cancel");click(app,"다음 문제");check(app.screen==CampusQuiz.Screen.INCORRECT,"Incorrect screen");click(app,"다음 문제");click(app,"답변 A: 학생회관 5층");click(app,"다음 문제");click(app,"다음 문제");click(app,"답변 A: 학생회관 2층");click(app,"다음 문제");click(app,"다음 문제");click(app,"답변 A: 제1강의동(진리관)");click(app,"다음 문제");click(app,"다음 문제");click(app,"답변 A: 제1복지관 3층");click(app,"다음 문제");check(app.screen==CampusQuiz.Screen.CORRECT,"Final campus feedback");click(app,"다음 문제");check(app.screen==CampusQuiz.Screen.PART_DONE,"Part completion");
            click(app,"다음 파트 시작하기");check(app.part==1,"Next part");click(app,"Part 02 시작하기");check(app.screen==CampusQuiz.Screen.PLATE_GAME&&app.flyingPlates.isEmpty(),"Academic game starts empty");app.stopPlateTimer();app.spawnFlyingPlate();double flyingBefore=app.flyingPlates.get(0).x;app.tickPlateGame();check(app.flyingPlates.get(0).x!=flyingBefore,"Plate movement");app.spawnPreviewPlates();app.rebuild();check(app.hotspots.stream().noneMatch(h->h.name().startsWith("접시")),"Plates use canvas aim");app.fireAt(app.flyingPlates.get(0).x,app.flyingPlates.get(0).y);check(app.plateRound==1&&app.partScore==0&&app.pendingPlateOutcome&&!app.outcomeCorrect,"Wrong plate is recorded");check(button(app,"다음 문제").isEnabled(),"Outcome requires next button");click(app,"게임 종료");check(app.screen==CampusQuiz.Screen.EXIT,"Game exit modal");click(app,"계속 풀기");check(app.screen==CampusQuiz.Screen.PLATE_GAME&&app.plateRound==1,"Resume game");click(app,"다음 문제");app.stopPlateTimer();app.spawnPreviewPlates();app.rebuild();shootCorrect(app);check(button(app,"다음 문제").isEnabled(),"Correct outcome requires next button");click(app,"다음 문제");check(app.plateRound==2&&app.partScore==10&&app.questionIndex==2,"Correct answer plate advances");for(int i=1;i<4;i++){app.spawnPreviewPlates();app.rebuild();shootCorrect(app);click(app,i==3?"결과 보기":"다음 문제");}check(app.screen==CampusQuiz.Screen.PART_DONE&&app.partScore==40,"Academic game completion");
            click(app,"다음 파트 시작하기");click(app,"Part 03 시작하기");click(app,"답변 A: 3강의동");click(app,"다음 문제");check(app.screen==CampusQuiz.Screen.INCORRECT,"Building incorrect screen");click(app,"다음 문제");click(app,"답변 A: 어울림관");click(app,"다음 문제");click(app,"다음 문제");click(app,"답변 A: 기숙사");click(app,"다음 문제");click(app,"다음 문제");click(app,"최종 결과 확인하기");check(app.screen==CampusQuiz.Screen.FINAL,"Final result");
            click(app,"오답 8문제 복습하기");click(app,"오답 복습 2");check(app.reviewIndex==1,"Review selection");click(app,"다음 오답 보기");check(app.reviewIndex==2,"Next review");click(app,"복습 목록으로");click(app,"최종 결과");click(app,"처음으로");check(app.screen==CampusQuiz.Screen.WELCOME&&app.part==0&&app.selected==-1,"Reset");
            app.startPart(0);click(app,"Part 01 시작하기");click(app,"퀴즈 종료");click(app,"종료하기");check(app.screen==CampusQuiz.Screen.PARTS&&app.selected==-1,"Exit discards sample selection");
            System.out.println("PASS: "+renders+" scaled screen renders, all assets, answer gating, modal cancellation/exit, three-part flow and review navigation.");
        });
    }
}
