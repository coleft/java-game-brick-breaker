import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BlockGame {

	
	static class MyFrame extends JFrame {
		
		// constant : uppercase underbar uppercase 방식으로 상수명 짓는다.
		static int BALL_WIDTH = 15;
		static int BALL_HEIGHT = 15;
		static int BLOCK_ROWS = 5;
		static int BLOCK_COLUMNS = 10;
		static int BLOCK_WIDTH = 40;
		static int BLOCK_HEIGHT = 20;
		static int BLOCK_GAP = 3;
		static int BAR_WIDTH = 80;
		static int BAR_HEIGHT = 20;
		static int CANVAS_WIDTH = 400 + (BLOCK_GAP * BLOCK_COLUMNS) - BLOCK_GAP;
		static int CANVAS_HEIGHT = 600;
		
		// variable
		static MyPanel myPanel = null;	//처음 쓸 때 빨간 줄 뜨는 건 아직 클래스를 통해 정의를 해주지 않았기 때문이다. 클래스 만들라.
		static int score = 0;
		static Timer timer = null;
		static Block[][] blocks = new Block[BLOCK_ROWS][BLOCK_COLUMNS];
		static Bar bar = new Bar();
		static Ball ball = new Ball();
		static int barXTarget = bar.x; //Target Value - interpolation
		static int dir = 0; //0 : Up-Right 1 : Down-Right 2 : Up-Left 3 : Down-Left
		static int ballSpeed = 5;
		
		static class Ball {
			int x = CANVAS_WIDTH/2 - BALL_WIDTH/2;
			int y = CANVAS_HEIGHT*1/2;
			int width = BALL_WIDTH;
			int height = BALL_HEIGHT;			
		}
		
		static class Bar {
			int x = CANVAS_WIDTH/2 - BAR_WIDTH/2;
			int y = CANVAS_HEIGHT*8/9;
			int width;
			int height;
		}
		
		static class Block {
			int x = 0;	//처음에 초기화 하기 애매하다 왜냐하면 블록 여러개가 다 달라서 코딩 따로 해주자 정 애매하면 0으로해 놔
			int y = 0;
			int width = BLOCK_WIDTH;
			int height = BLOCK_HEIGHT;
			int color = 0; //0:white 1:yellow 2:blue 3:mazanta(magenta) 4:red
			boolean isHidden = false; //after collision, block will be hidden.(메모리 제어보다 속성 제어가 편하다.)
		}
		
		static class MyPanel extends JPanel {	//CANVAS for Draw!
			public MyPanel() {
				this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
				this.setBackground(Color.BLACK);
			}
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2d = (Graphics2D)g; //형변환?
				
				drawUI( g2d );
			}
			private void drawUI(Graphics2D g2d) {
				//draw Blocks
				for(int i = 0; i<BLOCK_ROWS; i++) {
					for(int j = 0; j<BLOCK_COLUMNS; j++) {
						if(blocks[i][j].isHidden) {
							continue;
						}
						if(blocks[i][j].color == 0) {
							g2d.setColor(Color.WHITE);
						}
						else if(blocks[i][j].color == 1) {
							g2d.setColor(Color.YELLOW);
						}
						else if(blocks[i][j].color == 2) {
							g2d.setColor(Color.BLUE);
						}
						else if(blocks[i][j].color == 3) {
							g2d.setColor(Color.MAGENTA);
						}
						else if(blocks[i][j].color == 4) {
							g2d.setColor(Color.RED);
						}
						//실제 그리는 부분
						g2d.fillRect(blocks[i][j].x, blocks[i][j].y,
								blocks[i][j].width, blocks[i][j].height); //fillRect는 네모 그리는 함수임
					}
					
					//draw score
					g2d.setColor(Color.WHITE);
					g2d.setFont(new Font("OPTITimes-Roman", Font.BOLD, 20));
					g2d.drawString("score : ", CANVAS_WIDTH/2 - 30, 20);
					
					//draw Ball
					g2d.setColor(Color.WHITE);
					g2d.fillOval(ball.x, ball.y, BALL_WIDTH, BALL_HEIGHT);
					
					//draw bar
					g2d.setColor(Color.WHITE);
					g2d.fillRect(bar.x, bar.y, BAR_WIDTH, BAR_HEIGHT);
				}
			}
		}
		
		public MyFrame(String title) {
			super(title); //JFrame 쪽 생성자에 연결 해주는 것
			this.setVisible(true); //화면에 보이기 this = JFrame 임.
			this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
			this.setLocation(400, 300);
			this.setLayout(new BorderLayout());
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			initData();	//위에서 선언한 변수들을 초기화한다.(위에선 constant 등 선언만 한거야)
			
			myPanel = new MyPanel(); //캔버스 역할을 함
			this.add("Center", myPanel);
			
			setKeyListener();
			startTimer();
		}
		
		public void initData() {
			for(int i = 0; i<BLOCK_ROWS; i++) {
				for(int j = 0; j<BLOCK_COLUMNS; j++) {
					blocks[i][j] = new Block(); //위랑 비교 위는 공간만 만들고, 여기선 실제로 블락을 만든다.
					blocks[i][j].x = BLOCK_WIDTH * j + BLOCK_GAP * j;
					blocks[i][j].y = 100 + BLOCK_HEIGHT*i + BLOCK_GAP*i;
					blocks[i][j].width = BLOCK_WIDTH;
					blocks[i][j].height = BLOCK_HEIGHT;
					blocks[i][j].color = 4 - i; //위 색깔표 참조, 맨 위부터 흰색 시작
					blocks[i][j].isHidden = false; //여기까지 초기화 과정임
				}
			}
		}
		public void setKeyListener() {
			this.addKeyListener( new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {	//Key Event
					if( e.getKeyCode() == KeyEvent.VK_LEFT ) {
						System.out.println("Pressed Left Key");
						barXTarget -= 20;
						if( bar.x < barXTarget) {	//key press repeated...
							barXTarget = bar.x;
						}
					}
					else if( e.getKeyCode() == KeyEvent.VK_RIGHT) {
						System.out.println("Pressed Right Key");
						barXTarget += 20;
						if( bar.x > barXTarget) {	//key press repeated...
							barXTarget = bar.x;
						}
					}
				}
			});
		}
		public void startTimer() {	//움직임은 여기서
			timer = new Timer(20, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {	//Timer Event
					movement();
					checkCollision();
					checkCollisionBlock();
					myPanel.repaint(); //Redraw !!
				}
			});
			timer.start(); //Start Timer!
		}
		//이제 하나씩 함수 객체를 만들어 봅시다.
		public void movement() {
			
		}
		public void checkCollision() {
			
		}
		public void checkCollisionBlock() {
			
		}
	}
	
	public static void main(String[] args) {

		new MyFrame("Block Game");

	}

}
