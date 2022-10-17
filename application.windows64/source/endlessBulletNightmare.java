import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class endlessBulletNightmare extends PApplet {



boolean dead = false;
boolean score = false;
int timeSurvived = 0;
float hue = 0;
float maxHue = 5000;
int pulseTimer = 20;
boolean immune = false;
float immunityStart = 0;
float aura = 100;
int enemiesAmt = 0;
float playerX;
float playerY;
float playerSpeed = 50;
float playerTheta;
int item = 0;
int[] items = new int[]{5, 5, 2};
boolean teleportImmune = false;
float teleportImmunityStart = 0;
int teleportTimer = 0;
float smallEmpStart = 0;
boolean smallEmp = false;
int smallEmpTimer = 0;
boolean bigEmp = false;
int bigEmpTimer = 0;

ArrayList<Bullet> bullets = new ArrayList<Bullet>();
ArrayList<Enemy> enemies = new ArrayList<Enemy>();
ArrayList<Pulse> pulses = new ArrayList<Pulse>();
ArrayList<EmpBlast> empBlasts = new ArrayList<EmpBlast>();
ArrayList<EmpPulse> empPulses = new ArrayList<EmpPulse>();

class Bullet {
  float x, y, h, dx, dy;
  Bullet(float a, float b, float c, float d) {
    x = a;
    y = b;
    dx = c;
    dy = d;
    h = hue;
    hue ++;
    if (hue > maxHue) {
      hue = 0;
    }
  }
  public void render() {
    if (!score) {
      colorMode(RGB, 255);
      stroke(255);
      colorMode(HSB, maxHue);
      fill(h, maxHue, maxHue);
      ellipse(x, y, 10, 10);
      colorMode(RGB, 255);
      stroke(0);
    }
  }
  public void move() {
    if (teleportImmune) {
      x += dx / 5;
      y += dy / 5;
    } else {
      x += dx;
      y += dy;
    }
  }
}

abstract class Enemy {
  int shots, timer;
  float x, y, theta, pTheta, despawnTimer, spawnTimer;
  boolean firing = false;
  public abstract void fire();
  public void spawn() {
    if (spawnTimer < 255) {
      spawnTimer ++;
    }
  }
  public void despawn() {
    despawnTimer ++;
  }
  public void render() {
    if (!score) {
      colorMode(RGB, 255);
      strokeWeight(1);
      fill(255, min(pow(spawnTimer - despawnTimer, 2), 255));
      stroke(0, min(pow(spawnTimer - despawnTimer, 2), 255));
      ellipse(x, y, 20, 20);
    }
  }
  public void renderA() {
    if (!score) {
      strokeWeight(1);
      colorMode(HSB, maxHue);
      fill(hue, maxHue, maxHue, maxHue * min(pow(spawnTimer - despawnTimer, 2), 255) / (255 * 4));
      stroke(hue, maxHue, maxHue, maxHue * min(pow(spawnTimer - despawnTimer, 2), 255) / 255);
      strokeWeight(1);
      ellipse(x, y, 2 * aura * (min(pow((float) spawnTimer - despawnTimer, 2), 255)) / 255, 2 * aura * (min(pow((float) spawnTimer - despawnTimer, 2), 255)) / 255);
      colorMode(RGB, 255);
      strokeWeight(1);
    }
  }
}

class Spinner extends Enemy {
  Spinner(float a, float b) {
    x = a;
    y = b;
  }
  float theta = random(2 * PI);
  public void fire() {
    if (firing == false) {
      timer += 1;
      if (timer > 30) {
        if (random(10) < 1) {
          firing = true;
        }
        timer = 0;
      }
    }
    if (firing == true && timer > 180) {
      firing = false;
      timer = 0;
    }
    if (firing)
      timer ++;
    if (firing && timer % 8 == 0 && dist(x, y, playerX, playerY) > aura) {
      bullets.add(new Bullet(x, y, cos(theta), sin(theta)));
      bullets.add(new Bullet(x, y, cos(theta + PI / 2), sin(theta + PI / 2)));
      bullets.add(new Bullet(x, y, cos(theta + PI), sin(theta + PI)));
      bullets.add(new Bullet(x, y, cos(theta - PI / 2), sin(theta - PI / 2)));
      theta += 0.2f;
      if (theta > 2 * PI) {
        theta -= 2 * PI;
      }
    }
    if (firing && timer % 8 == 0)
      shots += 4;
  }
}

class Single extends Enemy {
  float aimSpeed = 0.03f;
  Single(float a, float b) {
    x = a;
    y = b;
    if (x < playerX) {
      theta = -asin((y - playerY) / dist(x, y, playerX, playerY));
    } else {
      theta = PI + asin((y - playerY) / dist(x, y, playerX, playerY));
    }
  }
  public void fire() {
    if (theta < 0) {
      theta += 2 * PI;
    }
    theta %= 2 * PI;
    if (x < playerX) {
      //reset angle to [0, 2pi]
      if (-asin((y - playerY) / dist(x, y, playerX, playerY)) < 0) {
        pTheta = -asin((y - playerY) / dist(x, y, playerX, playerY)) + 2 * PI;
      } else {
        pTheta = -asin((y - playerY) / dist(x, y, playerX, playerY));
      }
      //tracking code
      if (y > playerY) {
        if (theta > pTheta) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else if (pTheta - theta < PI) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        }
      } else {
        pTheta = -asin((y - playerY) / dist(x, y, playerX, playerY));
        if (theta < pTheta) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else if (theta - pTheta < PI) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        }
      }
    } else {
      pTheta = PI + asin((y - playerY) / dist(x, y, playerX, playerY));
      if (y < playerY) {
        if (theta < pTheta) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else if (theta - pTheta < PI) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        }
      } else {
        if (theta > pTheta) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else if (pTheta - theta < PI) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        }
      }
    }
    if (firing == false) {
      timer += 1;
      if (timer > 5) {
        if (random(10) < 1) {
          firing = true;
        }
        timer = 0;
      }
    }
    if (firing == true && timer > 16) {
      firing = false;
      timer = 0;
    }
    if (firing) {
      if (timer % 3 == 0 && dist(x, y, playerX, playerY) > aura) {
        bullets.add(new Bullet(x, y, cos(theta) * 3, sin(theta) * 3));
      }
      if (timer % 3 == 0)
        shots ++;
      timer ++;
    }
  }
}

class Spread extends Enemy {
  float aimSpeed = 0.03f;
  Spread(float a, float b) {
    x = a;
    y = b;
    if (x < playerX) {
      theta = -asin((y - playerY) / dist(x, y, playerX, playerY));
    } else {
      theta = PI + asin((y - playerY) / dist(x, y, playerX, playerY));
    }
  }
  public void fire() {
    if (theta < 0) {
      theta += 2 * PI;
    }
    theta %= 2 * PI;
    if (x < playerX) {
      //reset angle to [0, 2pi]
      if (-asin((y - playerY) / dist(x, y, playerX, playerY)) < 0) {
        pTheta = -asin((y - playerY) / dist(x, y, playerX, playerY)) + 2 * PI;
      } else {
        pTheta = -asin((y - playerY) / dist(x, y, playerX, playerY));
      }
      //tracking code
      if (y > playerY) {
        if (theta > pTheta) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else if (pTheta - theta < PI) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        }
      } else {
        pTheta = -asin((y - playerY) / dist(x, y, playerX, playerY));
        if (theta < pTheta) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else if (theta - pTheta < PI) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        }
      }
    } else {
      pTheta = PI + asin((y - playerY) / dist(x, y, playerX, playerY));
      if (y < playerY) {
        if (theta < pTheta) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else if (theta - pTheta < PI) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        }
      } else {
        if (theta > pTheta) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else if (pTheta - theta < PI) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        }
      }
    }
    if (firing == false) {
      timer += 1;
      if (timer > 5) {
        if (random(10) < 1) {
          firing = true;
        }
        timer = 0;
      }
    }
    if (firing == true && timer > 16) {
      firing = false;
      timer = 0;
    }
    if (firing) {
      if (timer % 12 == 0 && dist(x, y, playerX, playerY) > aura) {
        bullets.add(new Bullet(x, y, cos(theta - PI / 6) * 2, sin(theta - PI / 6) * 2));
        bullets.add(new Bullet(x, y, cos(theta - PI / 12) * 2, sin(theta - PI / 12) * 2));
        bullets.add(new Bullet(x, y, cos(theta + PI / 12) * 2, sin(theta + PI / 12) * 2));
        bullets.add(new Bullet(x, y, cos(theta + PI / 6) * 2, sin(theta + PI / 6) * 2));
      }
      if (timer % 12 == 0) {
        shots += 4;
      }
      timer ++;
    }
  }
}

class Wave extends Enemy {
  float aimSpeed = 0.03f;
  Wave(float a, float b) {
    x = a;
    y = b;
    if (x < playerX) {
      theta = -asin((y - playerY) / dist(x, y, playerX, playerY));
    } else {
      theta = PI + asin((y - playerY) / dist(x, y, playerX, playerY));
    }
  }
  public void fire() {
    if (theta < 0) {
      theta += 2 * PI;
    }
    theta %= 2 * PI;
    if (x < playerX) {
      //reset angle to [0, 2pi]
      if (-asin((y - playerY) / dist(x, y, playerX, playerY)) < 0) {
        pTheta = -asin((y - playerY) / dist(x, y, playerX, playerY)) + 2 * PI;
      } else {
        pTheta = -asin((y - playerY) / dist(x, y, playerX, playerY));
      }
      //tracking code
      if (y > playerY) {
        if (theta > pTheta) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else if (pTheta - theta < PI) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        }
      } else {
        pTheta = -asin((y - playerY) / dist(x, y, playerX, playerY));
        if (theta < pTheta) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else if (theta - pTheta < PI) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        }
      }
    } else {
      pTheta = PI + asin((y - playerY) / dist(x, y, playerX, playerY));
      if (y < playerY) {
        if (theta < pTheta) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else if (theta - pTheta < PI) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        }
      } else {
        if (theta > pTheta) {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        } else if (pTheta - theta < PI) {
          if (theta + aimSpeed > pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta += aimSpeed;
          }
        } else {
          if (theta - aimSpeed < pTheta && abs(pTheta - theta) < PI) {
            theta = pTheta;
          } else {
            theta -= aimSpeed;
          }
        }
      }
    }
    if (firing == false) {
      timer += 1;
      if (timer > 5) {
        if (random(10) < 1) {
          firing = true;
        }
        timer = 0;
      }
    }
    if (firing == true && timer > 16) {
      firing = false;
      timer = 0;
    }
    if (firing) {
      if (timer % 12 == 0 && dist(x, y, playerX, playerY) > aura) {
        bullets.add(new Bullet(x, y, cos(theta - PI / 30) * 3, sin(theta - PI / 30) * 3));
        bullets.add(new Bullet(x, y, cos(theta - PI / 60) * 3, sin(theta - PI / 60) * 3));
        bullets.add(new Bullet(x, y, cos(theta) * 3, sin(theta) * 3));
        bullets.add(new Bullet(x, y, cos(theta + PI / 60) * 3, sin(theta + PI / 60) * 3));
        bullets.add(new Bullet(x, y, cos(theta + PI / 30) * 3, sin(theta + PI / 30) * 3));
      }
      if (timer % 12 == 0) {
        shots += 5;
      }
      timer ++;
    }
  }
}

class Pulse {
  float x, y, radius;
  Pulse(float a, float b) {
    x = a;
    y = b;
    radius = 0;
  }
  public void render() {
    if (!score) {
      noFill();
      colorMode(HSB, maxHue);
      stroke(hue, maxHue, maxHue, 100 * maxHue / radius);
      strokeWeight(1);
      ellipse(x, y, radius, radius);
      colorMode(RGB, 255);
      radius ++;
    }
  }
}

class EmpBlast {
  float x, y, start;
  EmpBlast(float a, float b) {
    x = a;
    y = b;
    start = 1;
  }
  public void render() {
    colorMode(HSB, maxHue);
    fill(hue, maxHue, maxHue, maxHue * (pow(start - 1, 3) + 1) / 2);
    stroke(hue, maxHue, maxHue, maxHue * (pow(start - 1, 3) + 1) / 2);
    ellipse(x, y, 500, 500);
    start -= 0.05f;
    colorMode(RGB, 255);
  }
}

class EmpPulse {
  float x, y, radius;
  EmpPulse(float a, float b) {
    x = a;
    y = b;
    radius = 0;
  }
  public void render() {
    if (!score) {
      noFill();
      colorMode(HSB, maxHue);
      stroke(hue, maxHue, maxHue);
      strokeWeight(5);
      ellipse(x, y, 2 * radius, 2 * radius);
      colorMode(RGB, 255);
      radius += 3;
    }
  }
}

public int die(Bullet b, int i) {
  if (dist(playerX, playerY, b.x, b.y) < 30) {
    if (teleportImmune) {
      bullets.remove(b);
      i -= 1;
    }
  }
  if (dist(playerX, playerY, b.x, b.y) < 8) {
    if (immune) {
      bullets.remove(b);
      i -= 1;
      immune = false;
    } else {
      fill(0);
      textSize(50);
      if (!score) {
        text("You Died.", random(width), random(height));
      }
      dead = true;
    }
  }
  return i;
}

public void deco() {
  strokeWeight(20);
  colorMode(HSB, maxHue);
  stroke(hue, maxHue, maxHue);
  line(0, 0, width, 0);
  line(width, 0, width, height);
  line(width, height, 0, height);
  line(0, height, 0, 0);
  strokeWeight(1);
  colorMode(RGB, 255);
}

public void immunityAnimation() {
  colorMode(HSB, maxHue);
  stroke(hue, maxHue, maxHue);
  if (immunityStart < 1 && immune) {
    immunityStart += 0.05f;
  }
  if (immunityStart > 0 && !immune) {
    immunityStart -= 0.05f;
  }
  noFill();
  strokeWeight(3);
  ellipse(playerX, playerY, 30 * (pow(immunityStart - 1, 3) + 1), 30 * (pow(immunityStart - 1, 3) + 1));
  colorMode(RGB, 255);
}

public void teleportAnimation() {
  colorMode(HSB, maxHue);
  fill(hue, maxHue, maxHue);
  noStroke();
  if (teleportImmunityStart < 1 && teleportImmune) {
    teleportImmunityStart += 0.05f;
  }
  if (teleportImmunityStart > 0 && !teleportImmune) {
    teleportImmunityStart -= 0.05f;
  }
  ellipse(playerX, playerY, 30 * (pow(teleportImmunityStart - 1, 3) + 1), 30 * (pow(teleportImmunityStart - 1, 3) + 1));
  colorMode(RGB, 255);
}

public void smallEmpAnimation() {
  if (smallEmp) {
    empBlasts.add(new EmpBlast(playerX, playerY));
  }
  for (int i = 0; i < empBlasts.size(); i ++) {
    EmpBlast e = empBlasts.get(i);
    e.render();
    if (e.start <= 0) {
      empBlasts.remove(i);
      i --;
    }
  }
}

public void bigEmpAnimation() {
  if (bigEmp) {
    empPulses.add(new EmpPulse(playerX, playerY));
  }
  for (int i = 0; i < empPulses.size(); i ++) {
    EmpPulse e = empPulses.get(i);
    e.render();
    if (e.radius > (dist(0, 0, width, height))) {
      empPulses.remove(i);
      i --;
    }
  }
}

public void spawnEnemy() {
  switch((int) random(4)) {
  case 0:
    enemies.add(new Spinner((int) random(width), (int) random(height)));
    break;
  case 1:
    enemies.add(new Single((int) random(width), (int) random(height)));
    break;
  case 2:
    enemies.add(new Spread((int) random(width), (int) random(height)));
    break;
  case 3:
    enemies.add(new Wave((int) random(width), (int) random(height)));
  }
}

public void playerThetaCalc() {
  if (playerX < mouseX) {
    playerTheta = asin((mouseY - playerY) / dist(mouseX, mouseY, playerX, playerY));
  } else if (playerX > mouseX) {
    playerTheta = -asin((mouseY - playerY) / dist(mouseX, mouseY, playerX, playerY)) + PI;
  } else if (playerX == mouseX) {
    if (playerY < mouseY) {
      playerTheta = PI / 2;
    } else {
      playerTheta = PI + PI / 2;
    }
  }
}

public void move() {
  if (mouseX > 0 && mouseX < width && mouseY > 0 && mouseY < height) {
    if (dist(mouseX, mouseY, playerX, playerY) < 100) {
      playerX = mouseX;
      playerY = mouseY;
    } else {
      playerTheta += TAU;
      playerTheta %= TAU;
      playerX += cos(playerTheta) * playerSpeed;
      playerY += sin(playerTheta) * playerSpeed;
    }
  }
}

public void teleportCheck() {
  if (teleportTimer <= 0 && teleportImmune) {
    teleportImmune = false;
  }
  if (teleportTimer > 0) {
    teleportTimer -= 1;
  }
}

public void smallEmpCheck() {
  if (smallEmpTimer <= 0 && smallEmp) {
    smallEmp = false;
  }
  if (smallEmpTimer > 0) {
    smallEmpTimer -= 1;
  }
}

public void bigEmpCheck() {
  if (bigEmpTimer <= 0 && bigEmp) {
    bigEmp = false;
  }
  if (bigEmpTimer > 0) {
    bigEmpTimer -= 1;
  }
}

public void keyPressed() {
  if (key == 'a' || key == 'A') {
    item = 0;
  }
  if (key == 's' || key == 'S') {
    item = 1;
  }
  if (key == 'd' || key == 'D') {
    item = 2;
  }
}

public void mouseClicked() {
  if (!dead) {
    if (mouseButton == LEFT) {
      if (item == 0 && items[0] > 0) {
        teleportImmune = true;
        teleportTimer = 60;
        items[0] -= 1;
      }
      if (item == 1 && items[1] > 0) {
        smallEmp = true;
        smallEmpTimer = 1;
        items[1] -= 1;
      }
      if (item == 2 && items[2] > 0) {
        bigEmp = true;
        bigEmpTimer = 1;
        items[2] -= 1;
      }
    } else {
      enemiesAmt ++;
      spawnEnemy();
    }
  } else {
    score = true;
    background(255);
    textSize(20);
    text("Score: " + timeSurvived + " frames.", 300, 300);
  }
}

public void setup() {
  
  playerX = width / 2;
  playerY = height / 2;
  noCursor();
}

public void draw() {
  if (!dead) {
    background(255);
    colorMode(HSB, maxHue);
    fill(hue, maxHue, maxHue);
    playerThetaCalc();
    move();
    ellipse(playerX, playerY, 5, 5);
    colorMode(RGB, 255);
    if (immunityStart <= 0 && !immune) {
      immune = true;
    }
    immunityAnimation();
    teleportCheck();
    smallEmpCheck();
    bigEmpCheck();
    timeSurvived ++;
  }

  if (timeSurvived % 20 == 0) {
    pulses.add(new Pulse(playerX, playerY));
  }

  for (int i = 0; i < pulses.size(); i ++) {
    Pulse p = pulses.get(i);
    p.render();
    if (p.radius > 2 * dist(0, 0, width, height)) {
      pulses.remove(p);
      i --;
    }
  }

  if (enemies.size() < enemiesAmt) {
    spawnEnemy();
  }

  for (int i = 0; i < enemies.size(); i ++) {
    Enemy e = enemies.get(i);
    e.spawn();
    e.renderA();
    e.fire();
    if (e.shots > 100) {
      e.despawn();
      if (e.despawnTimer >= 255) {
        enemies.remove(e);
        i --;
      }
    }
  }

  for (Enemy e : enemies) {
    e.render();
  }

  for (int i = 0; i < bullets.size(); i ++) {
    Bullet b = bullets.get(i);
    b.move();
    if (b.x > width || b.x < 0 || b.y > height || b.y < 0) {
      bullets.remove(b);
      i --;
    }
    if (dist(playerX, playerY, b.x, b.y) <= 250 && smallEmp) {
      bullets.remove(b);
      i --;
    }
    for (EmpPulse e : empPulses) {
      if (dist(e.x, e.y, b.x, b.y) <= e.radius + 5) {
        bullets.remove(b);
        i --;
      }
    }
    i = die(b, i);
    b.render();
  }
  teleportAnimation();
  smallEmpAnimation();
  bigEmpAnimation();
  deco();
}
  public void settings() {  size(1000, 600, P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "endlessBulletNightmare" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
