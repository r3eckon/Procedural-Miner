package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.CpuSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.async.ThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Olivier on 8/2/2016.
 */
public class CGame {


    Cbuffer buffer;
    CWorld world;
    CPlayer player;

    long seed = 123;

    boolean lastmined = true;

    FreeWindow stats;

    Color[] lvlcolors = {Color.GRAY , Color.LIGHT_GRAY , Color.DARK_GRAY , Color.TEAL  ,Color.GOLD, Color.RED , Color.CORAL};

    public CGame(){
        player = new CPlayer("Sup");
        world = new CWorld(seed , 500);
        buffer = new Cbuffer(520 ,50 , world , player );
        stats = new FreeWindow(new Vector2(0 , 0 ) , 0 , 80 , 80 , true);
        stats.addElement(new FText("Hello" , new Vector2(-75 , 75) , stats.elements.size()));
        stats.addElement(new FRenderable(lvlcolors[0] , new Vector2(0,-2) , stats.elements.size()));
        lastmined = true;
    }

    public void update(){

        stats.update();
        stats.elements.get(0).setText("Seed : " + seed + "\nPosition : ( " + buffer.bx + " , " + buffer.by + " )\nScore : " + player.score + "\n\nNEXT : " + "\n\nMined : " + player.pick.hits);
        stats.elements.get(1).setColor(lvlcolors[player.pick.level]);
        buffer.update(true);

        player.x = buffer.bx;
        player.y = buffer.by;

        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            if(lastmined){
                buffer.player.x--;
                lastmined = buffer.mine();
            }


        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            if(lastmined){
                buffer.player.x++;
                lastmined = buffer.mine();
            }
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
            if(lastmined){
                buffer.player.y++;
                lastmined = buffer.mine();
            }
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
            if(lastmined){
                buffer.player.y--;
                lastmined = buffer.mine();
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            player = new CPlayer("sup");
            seed = ProceduralMiner.rand.nextInt();
            world = new CWorld(seed, 500);
            lastmined =  true;
            buffer = new Cbuffer(200 ,200 , world , player);
        }

    }

    public void draw(ShapeRenderer sr , ShapeRenderer hr , int x , int y , int size){
        buffer.drawBuffer(sr , x , y , size);
        stats.render(hr , ProceduralMiner.batch);
    }


}

class CPlayer {

    int x, y;
    String name;
    int level;
    int score;

    CPick pick;

    public CPlayer(String name){
        this.name = name;
        x = 0;
        y = 0;
        level = 0;
        score = 10;
        pick = new CPick();
        pick.level = pick.LVL_WOOD;
    }


}

class Cbuffer{
    int width , height;
    Block[][] blocks;
    int bx , by , ux , uy;
    CWorld world;
    CPlayer player;

    public Cbuffer(int w , int h , CWorld wd , CPlayer player){
        this.width = w;
        this.height = h;
        this.world = wd;
        blocks = new Block[w*2+1][h*2+1];
        this.player = player;
        update(true);
    }

    public void update(boolean force){

        blocks = getBlocks(bx , by , width , height);
        ux =bx;
        uy = by;
        move(player.x , player.y);


    }
    public void move(int x , int y){

        if(x < -world.width+1 || x > world.width-3){
            return;
        }

        this.bx = x;
        this.by = y;

        if(world.get(x , y-1) == BlockType.Empty){
            if(player.pick.level == CPick.LVL_LAVA){
                mine();
            }else{
                mine();
                move(x , y-1);
            }
        }
        if(world.get(x , y+1) == BlockType.Empty){
            if(player.pick.level == CPick.LVL_LAVA){
                mine();
                move(x , y+1);
            }else{
                mine();
            }
        }





    }

    public Block[][] getBlocks(int x , int y , int w , int h){

        Block[][] blocks = new Block[w*2+1][h*2+1];

        for(int i =  - w ; i < w ; i++){
            for(int j =  - h ; j < h ; j++){

                CPoint point = new CPoint(i,j);

                if(world.isMined(point) ){
                    blocks[i+w][j+h] = new Block(BlockType.Empty ,point );
                }

                blocks[i+w][j+h] = new Block(world.get(x+i,y+j) , new CPoint(x+i,y+j));
            }
        }

        return blocks;




    }

    public void drawBuffer(ShapeRenderer sr , int x , int y , int ds ){
        sr.begin();
        sr.set(ShapeRenderer.ShapeType.Filled);
        for(int i = -width ; i < width ; i++) {
            for (int j =  -height; j < height; j++) {


                    sr.set(ShapeRenderer.ShapeType.Filled);
                    sr.setColor(blocks[i+width][j+height].color);
                    sr.rect((x+i)*ds , (y+j)*ds , ds , ds);




                if(bx + i == player.x && by + j == player.y){
                    sr.set(ShapeRenderer.ShapeType.Filled);
                    sr.setColor(Color.RED);
                    sr.rect((x+i)*ds , (y+j)*ds , ds , ds);

                }


            }
        }
        sr.end();



    }

    public boolean mine(){

       return world.mine(new CPoint(player.x , player.y) , player);

    }


}

class CWorld{

    Random r;
    long seed;

    int width;

    ArrayList<CPoint>[] mined;

    int MOON = 1000;
    int CLOUDS = 100;
    int EASYLAYER = -100;
    int MEDIUMLAYER = -250;
    int HARDLAYER = -666;
    int EXTREMELAYER = -999;
    int HELL = -1010;
    int HELLANDBACK = -2020;
    int THEBED = -3000;
    int THECORE = -4000;
    int THEEND = -5000;





    public CWorld(long s  , int w){

        this.width = w;

        mined = new ArrayList[width*2];
        seed = s;
        r = new Random(s);

        initMineMap();




    }

    void initMineMap(){
        for(int i = 0 ; i < width*2 ; i++){
            mined[i] = new ArrayList<CPoint>(10000);
        }
    }

    boolean outOfBounds(int x , int y){
        return (x < -width || x >= width-1 || y > 100) ? true:false;

    }


    public BlockType get(int x , int y ){
        long newseed = (seed*x*y + seed*y*x) * (x-seed/2*y + y-seed/4*x) + (y-seed/4*y + x-seed/2*x) +  (x-seed/4*x - x-seed/2*x);
        r = new Random(newseed);

        int roll = r.nextInt(100);

        //Above / Next to world
        if(x < -width-1 || x > width){
            return BlockType.Dirt;
        }
        if(isMined(new CPoint(x , y))){
            return BlockType.Empty;
        }

        if(y>MOON){

        }else if(y>CLOUDS){

        }
        else if(y>EASYLAYER){
            if(roll % 2 == 0  || roll % 3 == 0 ){
                return BlockType.Dirt;
            }
            if(roll % 7 == 0 ) return BlockType.Stone;
            if((roll % 17 == 0  ))return BlockType.Iron;
            if(roll %37 ==0)return BlockType.Silicon;
            if(roll % 53 == 0 ) return BlockType.Gold;
            if(roll % 51 == 0 ) return BlockType.Silver;


        }else if(y > MEDIUMLAYER){
            if(roll % 4 == 0  || roll % 2 == 0 ){
                return BlockType.Dirt;
            }
            if(roll % 7 ==0 ) return BlockType.Stone;
            if(roll % 17 == 0)   return BlockType.Iron;
            if(roll % 31 == 0 ) return BlockType.Silver;
            if(roll % 23 == 0) return BlockType.Silicon;
            if(roll % 27 == 0) return BlockType.Magnesium;
        }else if(y > HARDLAYER){
            if(roll % 4 == 0  || roll % 3 == 0 ){
                return BlockType.Dirt;
            }
            if( roll % 7 ==0  ) return BlockType.Stone;
            if((roll % 17 == 0  ) ) return BlockType.Iron;
            if(roll % 19 == 0 ) return BlockType.Silver;
            if(roll % 17 == 0) return BlockType.Silicon;
            if(roll % 21 == 0) return BlockType.Magnesium;
            if(roll % 23 == 0) return BlockType.Uranium;
            if(roll % 27 == 0) return BlockType.Diamond;

        }else if(y > EXTREMELAYER){


            if(roll % 5 == 0   ){
                return BlockType.Dirt;
            }
            if(roll % 27 == 0 ) return BlockType.Gold;
            if(roll % 37 == 0) return BlockType.Ruby;

        }else if( y > HELL){
            return BlockType.Heaven;
        }else if(y> HELLANDBACK){
            if(roll % 2 == 0){
                return BlockType.Empty;
            }else{
                if(roll % 3 == 0) return BlockType.Heaven;
                if(roll % 5 == 0) return BlockType.Diamond;
                if(roll % 7 == 0) return BlockType.Gold;
            }
        }else if(y>THEBED){
            if(roll%2==0){
                return BlockType.Dirt;
            }else{
                if(roll % 5 == 0) return BlockType.Bedrock;
                if(roll % 27 == 0) return BlockType.Lava;

            }

        }else if(y > THECORE){
            if(roll % 3 == 0){
                return BlockType.Bedrock;
            }else{
                if(roll % 5 == 0)return BlockType.Dirt;
                if(roll % 7 == 0)return BlockType.Stone;
            }
        }else if(y > THEEND){
            if(roll % 3 == 0){
                return BlockType.Bedrock;
            }else{
                if(roll % 5 == 0)return BlockType.Lava;
            }
        }else{
            if(roll%2==0)return BlockType.Bedrock;
            if(roll%19==0)return BlockType.Stone;
        }

        return BlockType.Empty;

    }

    public boolean isMined(CPoint p){
        if(outOfBounds(p.x , 0)) return true;
        for(CPoint p2 : mined[p.x+width]){
            if(p.x == p2.x && p.y == p2.y) return true;
            else continue;
        }
        return false;
    }

    public boolean mine(CPoint point , CPlayer player){

        if(outOfBounds(point.x , point.y)) return  false;

        Block b = new Block(get(point.x , point.y) , new CPoint(point.x , point.y));


        if(b.type == BlockType.Empty && (player.score-=b.hp) < 0) return false;
        else {
            if(player.pick.level>=b.minelvl) {

                if(b.type == BlockType.Dirt){
                    player.score-=b.hp;
                }
                if(b.type != BlockType.Empty){
                    player.pick.hits++;
                }

                if(b.addlvl!=0 && player.pick.level < b.addlvl)
                    player.pick.level = b.addlvl;
                player.score+=b.value;

                mined[point.x+width].add(point);
                return true;
            }else {
                if((player.score-=b.value) < 0)
                    return false;
                else return true;
            }

        }

    }


}
class Block{

    BlockType type;

    CPoint point;

    int hp;
    int value;
    short minelvl;
    short addlvl;

    Color color;

    public Block(BlockType type , CPoint p){

        point = p;
        this.type = type;

        switch (type){
            case Empty:
                hp = 0;
                value = 0;
                color = Color.BLACK;
                minelvl = CPick.LVL_WOOD;
                addlvl = 0;
                break;
            case Dirt:
                hp = 0;
                value = -1;
                color =Color.BROWN;
                minelvl = CPick.LVL_WOOD;
                addlvl = 0;
                break;
            case Stone:
                hp = 2;
                value =1;
                color = Color.GRAY;
                minelvl = CPick.LVL_WOOD;
                addlvl = CPick.LVL_STONE;
                break;
            case Iron:
                hp = 2;
                value = 2;
                color = Color.LIGHT_GRAY;
                minelvl = CPick.LVL_STONE;
                addlvl = CPick.LVL_IRON;
                break;
            case Silicon:
                hp = 5;
                value =4;
                color = Color.DARK_GRAY;
                minelvl = CPick.LVL_IRON;
                addlvl = CPick.LVL_SILICON;
                break;
            case Gold:
                hp = 10;
                value =50;
                color = Color.YELLOW;
                minelvl = CPick.LVL_ALLOY;
                addlvl = 0;
                break;
            case Uranium:
                hp = 7;
                value = 75;
                color = Color.GREEN;
                minelvl = CPick.LVL_ALLOY;
                addlvl = 0;
                break;
            case Magnesium:
                hp = 3;
                value = 25;
                color = Color.ORANGE;
                minelvl = CPick.LVL_IRON;
                addlvl = 0;
                break;
            case Diamond:
                hp = 100;
                value = 100;
                color = Color.SKY;
                minelvl = CPick.LVL_ALLOY;
                addlvl = CPick.LVL_DIAMOND;
                break;
            case Silver:
                hp = 15;
                value = 15;
                color = Color.TEAL;
                minelvl = CPick.LVL_SILICON;
                addlvl = CPick.LVL_ALLOY;
                break;
            case Ruby:
                hp = 100;
                value = 111;
                color = Color.FIREBRICK;
                minelvl = CPick.LVL_DIAMOND;
                addlvl = 0;
                break;
            case Heaven:
                hp = 100;
                value = 100;
                color = Color.WHITE;
                minelvl = CPick.LVL_DIAMOND+3;
                addlvl = 0;
                break;
            case Cloud:
                hp = 10;
                value = 10;
                color = Color.CORAL;
                minelvl = CPick.LVL_LAVA;
                addlvl = CPick.LVL_CLOUD;
                break;
            case Ice:
                hp = 1;
                value = 1;
                color = Color.SKY;
                minelvl = CPick.LVL_LAVA;
                addlvl = 0;
                break;
            case Lava:
                hp = 10;
                value = -10;
                color = Color.RED;
                minelvl = CPick.LVL_DIAMOND;
                addlvl = CPick.LVL_LAVA;
                break;
            case Bedrock:
                hp = 1000;
                value = -1000;
                color = Color.DARK_GRAY;
                minelvl = CPick.LVL_WOOD;
                addlvl = 0;
                break;
            default:
                break;
        }
    }

}

class CPoint{

    public int x , y;

    public CPoint(int x , int y){
        this.x = x;
        this.y = y;

    }

}

enum BlockType{

   Empty , Dirt , Stone , Iron , Silicon , Magnesium , Gold , Silver , Uranium , Diamond , Ruby ,Heaven , Cloud , Ice , Lava , Bedrock ;

    int hp;
    public int value;

    Color color;



}
class CPick{

    public static final short LVL_WOOD = 0;
    public static final short LVL_STONE = 1;
    public static final short LVL_IRON = 2;
    public static final short LVL_SILICON = 3;
    public static final short LVL_ALLOY = 4;
    public static final short LVL_DIAMOND = 5;
    public static final short LVL_LAVA = 6;
    public static final short LVL_CLOUD =7;

    public short level;
    public int hits;



}

