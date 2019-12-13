package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FlappyJump extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture birdUp, birdDown, wall;
	private OrthographicCamera camera;
	private Bird bird;
	private final float gravity = -2;
	private float titleGravity = -1.5f;

	private FreeTypeFontGenerator generator;
	private FreeTypeFontGenerator.FreeTypeFontParameter parameter;
	private BitmapFont text100, text200, text500;

	private boolean useGravity, titleTouched;
	private float birdLocationOnTitleTouch;
	private boolean title, game, gameOver, startAnimation, gameOverAnimation;
	private boolean animate;
	private float[] titleTopTextPos = new float[2], titleBottomTextPos = new float[2], goTopTextPos = new float[2], goBottomTextPos = new float[2];
	private int animationTimer;
	private int score;

	private Wall[] walls;

	@Override
	public void create() {
		score = 0;
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(false);
		batch = new SpriteBatch();
		birdUp = new Texture("bird_up.png");
		birdDown = new Texture("bird_down.png");
		wall = new Texture("wall.png");
		bird = new Bird(156.25f, 125);
		bird.speed = 5; //starting speed for title screen animation

		useGravity = false;
		titleTouched = false;
		animate = false;

		title = true;
		startAnimation = false;
		game = false;
		gameOver = false;
		gameOverAnimation = false;

		generator = new FreeTypeFontGenerator(Gdx.files.internal("bird.ttf"));
		parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.borderColor = Color.BLACK;
		parameter.borderWidth = 10;
		parameter.size = 100;
		text100 = generator.generateFont(parameter);
		parameter.size = 200;
		text200 = generator.generateFont(parameter);
		parameter.size = 500;
		text500 = generator.generateFont(parameter);

		titleTopTextPos[0] = 500;
		titleTopTextPos[1] = 1000;
		titleBottomTextPos[0] = 485;
		titleBottomTextPos[1] = 200;

		walls = new Wall[10];
		resetWalls();

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.5f, 0, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		if (title) {
			batch.begin();
			if (!titleTouched) {
				bird.accelerate(titleGravity);
				if (bird.hitbox.y <= 200 && titleGravity < 0) {
					bird.speed = 40;
				}
			} else {
				if (birdLocationOnTitleTouch > 500) {
					if (bird.hitbox.y <= 500) {
						bird.speed = 0;
						bird.hitbox.y = 500;
					} else bird.speed = -7;
				} else {
					if (bird.hitbox.y >= 500) {
						bird.speed = 0;
						bird.hitbox.y = 500;
					} else bird.speed = 7;
				}
				if (bird.hitbox.x > 275) {
					bird.hitbox.x -= 12;
				}
				else bird.hitbox.x = 275;
				titleTopTextPos[0] += 15;
				titleBottomTextPos[0] += 15;
				if (titleTopTextPos[0] >= 2100 && bird.hitbox.x == 275) {
					title = false;
					startAnimation = true;
					animationTimer = 0;
				}
			}
			text200.draw(batch, "Floopy Jump!", titleTopTextPos[0], titleTopTextPos[1]);
			text200.draw(batch, "Click to Play!", titleBottomTextPos[0], titleBottomTextPos[1]);
			drawBird();
			if (Gdx.input.justTouched()) {
				titleTouched = true;
				birdLocationOnTitleTouch = bird.hitbox.y;
			}
			batch.end();
		} else if (startAnimation) {
			batch.begin();
			if (animationTimer >= 200) {
				useGravity = true;
				animationTimer = 0;
				startAnimation = false;
				game = true;
			} else if (animationTimer >= 150) {
				text500.draw(batch, "GO!", 800, 700);
			} else if (animationTimer >= 100) {
				text500.draw(batch, "1", 875, 700);
			} else if (animationTimer >= 50) {
				text500.draw(batch, "2", 875, 700);
			} else {
				text500.draw(batch, "3", 875, 700);
			}
			animationTimer++;
			drawBird();
			batch.end();
		} else if (game) {
			batch.begin();
			drawBird();
			drawWalls();
			text100.draw(batch, "Score:" + score, 10, 1065);
			batch.end();
			bird.accelerate(gravity);
			if (Gdx.input.justTouched()) {
				bird.speed = 25;
			}
			if (bird.hitbox.x > walls[score % 5].hitbox.x + walls[score % 5].hitbox.width) {
				score++;
			}
			for (int i = 0; i < 10; i++) {
				walls[i].hitbox.x -= 8;
				if (bird.hitbox.overlaps(walls[i].hitbox) || bird.hitbox.y <= 0) {
					game = false;
					bird.speed = 15;
					animationTimer = 0;
					gameOverAnimation = true;
				}
			}
		} else if (gameOverAnimation) {
			batch.begin();
			drawWalls();
			text100.draw(batch, "Score:" + score, 10, 1065);
			drawBird();
			batch.end();
			if (bird.hitbox.y > 0) {
				bird.accelerate(gravity);
			} else if (animationTimer <= 200) {
				animationTimer++;
			} else {
				goTopTextPos[0] = 100;
				goTopTextPos[1] = 900;
				goBottomTextPos[0] = 650;
				goBottomTextPos[1] = 600;
				bird.speed = 15;
				gameOverAnimation = false;
				gameOver = true;
			}
		} else if (gameOver) {
			batch.begin();
			drawWalls();
			text500.draw(batch, "GAME OVER!", goTopTextPos[0], goTopTextPos[1]);
			text200.draw(batch, "Score:" + score, goBottomTextPos[0], goBottomTextPos[1]);
			drawBird();
			if (!animate) {
				batch.draw(wall, 250, 200, 600, 200);
				batch.draw(wall, 1150, 200, 600, 200);
				text100.draw(batch, "Play Again!", 320, 320);
				text100.draw(batch, "Submit Score", 1175, 320);
			}
			batch.end();

			if (animate) {
			    if(birdLocationOnTitleTouch > 6000){
			        bird.speed = 0;
			        bird.hitbox.y = 500;
                }
				else if (birdLocationOnTitleTouch > 500) {
					if (bird.hitbox.y <= 500) {
						bird.speed = 0;
						bird.hitbox.y = 500;
					} else bird.speed = -20;
				} else {
					if (bird.hitbox.y >= 500) {
						bird.speed = 0;
						bird.hitbox.y = 500;
					} else bird.speed = 9;
				}
				goTopTextPos[0] += 10;
				goBottomTextPos[0] += 10;
				if (goTopTextPos[0] >= 2100 && bird.hitbox.x <= 275 && bird.hitbox.y == 500) {
					animate = false;
					gameOver = false;
					startAnimation = true;
					animationTimer = 0;
					score = 0;
				}
			}
			if (Gdx.input.justTouched()) {
				int x = Gdx.input.getX();
				int y = Gdx.input.getY();
				if (x >= 245 && x <= 845 && y >= 680 && y <= 880 && !animate) {
					animate = true;
					birdLocationOnTitleTouch = bird.hitbox.y;
					resetWalls();
				}
				if (x >= 1150 && x <= 1750 && y >= 680 && y <= 880) {

				}
			}
		}
		bird.move();
	}

	private void resetWalls() {
		for (int i = 0; i < 5; i++) {
			walls[i] = new Wall();
			walls[i].hitbox.x = Gdx.graphics.getWidth() + (i * 1300);
			walls[i].hitbox.y = 0;
			walls[i].hitbox.width = 170;
			walls[i].hitbox.height = (float) (Math.random() * 500) + 175;
		}

		for (int i = 5; i < 10; i++) {
			walls[i] = new Wall();
			walls[i].hitbox.x = walls[i - 5].hitbox.x;
			walls[i].hitbox.y = walls[i - 5].hitbox.height + 375;
			walls[i].hitbox.width = 170;
			walls[i].hitbox.height = 5000;
		}
	}

	@Override
	public void dispose() {
		batch.dispose();
		birdUp.dispose();
		birdDown.dispose();
		wall.dispose();
		text100.dispose();
		text200.dispose();
		text500.dispose();
	}

	public Texture birdType() {
		return bird.speed > 0 ? birdUp : birdDown;
	}

	public void drawBird() {
		batch.draw(birdType(), bird.hitbox.x, bird.hitbox.y, bird.hitbox.width, bird.hitbox.height);
	}

	public void drawWalls() {
		for (int i = 0; i < 10; i++) {
			batch.draw(wall, walls[i].hitbox.x, walls[i].hitbox.y, walls[i].hitbox.width, walls[i].hitbox.height);
			if (walls[i].hitbox.x < -200) {
				walls[i] = new Wall();
				if (i < 5) {
					walls[i].hitbox.x = walls[i + 4 % 5].hitbox.x + 1300;
					walls[i].hitbox.y = 0;
					walls[i].hitbox.width = 170;
					walls[i].hitbox.height = (float) (Math.random() * 500) + 175;
				} else {
					walls[i].hitbox.x = walls[i - 5].hitbox.x;
					walls[i].hitbox.y = walls[i - 5].hitbox.height + 350;
					walls[i].hitbox.width = 170;
					walls[i].hitbox.height = 5000;
				}
			}
		}
	}
}