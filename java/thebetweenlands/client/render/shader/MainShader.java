package thebetweenlands.client.render.shader;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;
import thebetweenlands.client.render.shader.base.CShader;
import thebetweenlands.client.render.shader.base.CShaderGroup;
import thebetweenlands.client.render.shader.base.CShaderInt;
import thebetweenlands.client.render.shader.effect.SwirlEffect;

public class MainShader extends CShader {
	private Framebuffer depthBuffer;
	private Framebuffer blitBuffer;
	private List<LightSource> lightSources = new ArrayList<LightSource>();
	private Matrix4f INVMVP;
	private Matrix4f MVP;
	private Matrix4f MV;
	private Matrix4f PM;
	private FloatBuffer mvBuffer = GLAllocation.createDirectFloatBuffer(16);
	private FloatBuffer pmBuffer = GLAllocation.createDirectFloatBuffer(16);
	private Map<String, GeometryBuffer> geometryBuffers = new HashMap<String, GeometryBuffer>();

	public MainShader(TextureManager textureManager,
			IResourceManager resourceManager, Framebuffer frameBuffer,
			ResourceLocation shaderDescription, ResourceLocation shaderPath,
			ResourceLocation assetsPath) {
		super(textureManager, resourceManager, frameBuffer, shaderDescription,
				shaderPath, assetsPath);
	}

	public GeometryBuffer getGeometryBuffer(String samplerName) {
		GeometryBuffer geomBuffer = this.geometryBuffers.get(samplerName);
		if(geomBuffer == null) {
			geomBuffer = new GeometryBuffer(true);
			this.geometryBuffers.put(samplerName, geomBuffer);
		}
		return geomBuffer;
	}

	/**
	 * Returns a fullscreen blit FBO
	 * @return
	 */
	public Framebuffer getBlitBuffer() {
		return this.blitBuffer;
	}

	public void addLight(LightSource light) {
		this.lightSources.add(light);
	}

	public void clearLights() {
		this.lightSources.clear();
	}

	public Framebuffer getDepthBuffer() {
		return this.depthBuffer;
	}

	public void deleteBuffers() {
		this.depthBuffer.deleteFramebuffer();
		for(GeometryBuffer gBuffer : this.geometryBuffers.values()) {
			gBuffer.deleteBuffers();
		}
	}

	public void updateBuffers(Framebuffer input) {
		if(this.depthBuffer == null) {
			this.depthBuffer = new Framebuffer(input.framebufferWidth, input.framebufferHeight, false);
			this.updateSampler("diffuse_depth", this.depthBuffer);
		}
		if(input.framebufferWidth != this.depthBuffer.framebufferWidth
				|| input.framebufferHeight != this.depthBuffer.framebufferHeight) {
			this.depthBuffer.deleteFramebuffer();
			this.depthBuffer = new Framebuffer(input.framebufferWidth, input.framebufferHeight, false);
			this.updateSampler("diffuse_depth", this.depthBuffer);
		}
		input.bindFramebuffer(false);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.depthBuffer.framebufferTexture);
		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, 0, 0, 
				this.depthBuffer.framebufferTextureWidth, 
				this.depthBuffer.framebufferTextureHeight, 
				0);

		if(this.blitBuffer == null) {
			this.blitBuffer = new Framebuffer(input.framebufferWidth, input.framebufferHeight, false);
		}
		if(input.framebufferWidth != this.blitBuffer.framebufferWidth
				|| input.framebufferHeight != this.blitBuffer.framebufferHeight) {
			this.blitBuffer.deleteFramebuffer();
			this.blitBuffer = new Framebuffer(input.framebufferWidth, input.framebufferHeight, false);
		}

		for(Entry<String, GeometryBuffer> geomBufferEntry : this.geometryBuffers.entrySet()) {
			geomBufferEntry.getValue().update(input);
			String samplerName = geomBufferEntry.getKey();
			Framebuffer geomBuffer = geomBufferEntry.getValue().getGeometryBuffer();
			Framebuffer geomDepthBuffer = geomBufferEntry.getValue().getGeometryDepthBuffer();
			this.updateSampler(samplerName, geomBuffer);
			if(geomDepthBuffer != null) {
				this.updateSampler(samplerName + "_depth", geomDepthBuffer);
			}
		}

		input.bindFramebuffer(false);
	}

	@Override
	public void updateShader(CShaderInt shader) {
		this.uploadMatrices(shader);
		this.uploadLights(shader);
		this.uploadMisc(shader);
	}

	private void uploadMisc(CShaderInt shader) {
		{
			ShaderUniform uniform = shader.getUniform("u_zNear");
			if(uniform != null) {
				uniform.func_148090_a(0.05F);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_zFar");
			if(uniform != null) {
				uniform.func_148090_a((float)(Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16) * 2.0F);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_camPos");
			if(uniform != null) {
				uniform.func_148095_a(
						(float)(RenderManager.renderPosX),
						(float)(RenderManager.renderPosY),
						(float)(RenderManager.renderPosZ));
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_msTime");
			if(uniform != null) {
				uniform.func_148090_a(System.nanoTime() / 1000000.0F);
			}
		}
	}

	private void uploadMatrices(CShaderInt shader) {
		{
			ShaderUniform uniform = shader.getUniform("u_INVMVP");
			if(uniform != null) {
				uniform.func_148088_a(this.INVMVP);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_MVP");
			if(uniform != null) {
				uniform.func_148088_a(this.MVP);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_MV");
			if(uniform != null) {
				uniform.func_148088_a(this.MV);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_PM");
			if(uniform != null) {
				uniform.func_148088_a(this.PM);
			}
		}
	}

	private static final Comparator<LightSource> lightSourceSorter = new Comparator<LightSource>(){
		@Override
		public int compare(LightSource o1, LightSource o2) {
			double dx1 = o1.x - RenderManager.renderPosX;
			double dy1 = o1.y - RenderManager.renderPosY;
			double dz1 = o1.z - RenderManager.renderPosZ;
			double dx2 = o2.x - RenderManager.renderPosX;
			double dy2 = o2.y - RenderManager.renderPosY;
			double dz2 = o2.z - RenderManager.renderPosZ;
			double d1 = Math.sqrt(dx1*dx1 + dy1*dy1 + dz1*dz1);
			double d2 = Math.sqrt(dx2*dx2 + dy2*dy2 + dz2*dz2);
			if(d1 > d2) {
				return 1;
			} else if(d1 < d2) {
				return -1;
			}
			return 0;
		}
	};

	private void uploadLights(CShaderInt shader) {
		//Sorts lights by distance
		Collections.sort(this.lightSources, lightSourceSorter);

		{
			ShaderUniform uniform = shader.getUniform("u_lightColorsR");
			if(uniform != null) {
				float[] posArray = new float[32];
				for(int i = 0; i < this.lightSources.size(); i++) {
					if(i >= 32) break;
					posArray[i] = (float)(this.lightSources.get(i).r);
				}
				uniform.func_148097_a(posArray);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_lightColorsG");
			if(uniform != null) {
				float[] posArray = new float[32];
				for(int i = 0; i < this.lightSources.size(); i++) {
					if(i >= 32) break;
					posArray[i] = (float)(this.lightSources.get(i).g);
				}
				uniform.func_148097_a(posArray);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_lightColorsB");
			if(uniform != null) {
				float[] posArray = new float[32];
				for(int i = 0; i < this.lightSources.size(); i++) {
					if(i >= 32) break;
					posArray[i] = (float)(this.lightSources.get(i).b);
				}
				uniform.func_148097_a(posArray);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_lightSourcesX");
			if(uniform != null) {
				float[] posArray = new float[32];
				for(int i = 0; i < this.lightSources.size(); i++) {
					if(i >= 32) break;
					posArray[i] = (float)(-RenderManager.renderPosX + this.lightSources.get(i).x);
				}
				uniform.func_148097_a(posArray);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_lightSourcesY");
			if(uniform != null) {
				float[] posArray = new float[32];
				for(int i = 0; i < this.lightSources.size(); i++) {
					if(i >= 32) break;
					posArray[i] = (float)(-RenderManager.renderPosY + this.lightSources.get(i).y);
				}
				uniform.func_148097_a(posArray);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_lightSourcesZ");
			if(uniform != null) {
				float[] posArray = new float[32];
				for(int i = 0; i < this.lightSources.size(); i++) {
					if(i >= 32) break;
					posArray[i] = (float)(-RenderManager.renderPosZ + this.lightSources.get(i).z);
				}
				uniform.func_148097_a(posArray);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_lightRadii");
			if(uniform != null) {
				float[] posArray = new float[32];
				for(int i = 0; i < this.lightSources.size(); i++) {
					if(i >= 32) break;
					posArray[i] = (float)(this.lightSources.get(i).radius);
				}
				uniform.func_148097_a(posArray);
			}
		}
		{
			ShaderUniform uniform = shader.getUniform("u_lightSources");
			if(uniform != null) {
				int count = this.lightSources.size();
				if(count > 32) {
					count = 32;
				}
				uniform.func_148090_a(count);
			}
		}
	}

	public void updateMatrices() {
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, mvBuffer);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, pmBuffer);
		org.lwjgl.util.vector.Matrix4f modelviewMatrix = (org.lwjgl.util.vector.Matrix4f) new org.lwjgl.util.vector.Matrix4f().load(mvBuffer.asReadOnlyBuffer());
		org.lwjgl.util.vector.Matrix4f invModelviewMatrix = (org.lwjgl.util.vector.Matrix4f) new org.lwjgl.util.vector.Matrix4f().load(mvBuffer.asReadOnlyBuffer()).invert();
		this.MV = this.toVecMathMatrix(modelviewMatrix);
		org.lwjgl.util.vector.Matrix4f projectionMatrix = (org.lwjgl.util.vector.Matrix4f) new org.lwjgl.util.vector.Matrix4f().load(pmBuffer.asReadOnlyBuffer());
		this.PM = this.toVecMathMatrix(projectionMatrix);
		org.lwjgl.util.vector.Matrix4f MVP = new org.lwjgl.util.vector.Matrix4f();
		org.lwjgl.util.vector.Matrix4f.mul(projectionMatrix, modelviewMatrix, MVP);
		this.MVP = this.toVecMathMatrix(MVP);
		MVP.invert();
		this.INVMVP = this.toVecMathMatrix(MVP);
	}

	private Matrix4f toVecMathMatrix(org.lwjgl.util.vector.Matrix4f mat) {
		Matrix4f vecMath = new Matrix4f();
		vecMath.m00 = mat.m00;
		vecMath.m01 = mat.m01;
		vecMath.m02 = mat.m02;
		vecMath.m03 = mat.m03;
		vecMath.m10 = mat.m10;
		vecMath.m11 = mat.m11;
		vecMath.m12 = mat.m12;
		vecMath.m13 = mat.m13;
		vecMath.m20 = mat.m20;
		vecMath.m21 = mat.m21;
		vecMath.m22 = mat.m22;
		vecMath.m23 = mat.m23;
		vecMath.m30 = mat.m30;
		vecMath.m31 = mat.m31;
		vecMath.m32 = mat.m32;
		vecMath.m33 = mat.m33;
		return vecMath;
	} 

	private SwirlEffect swirlEffect = null;
	private float swirlAngle = 0.0F;
	private float lastSwirlAngle = 0.0F;

	public void setSwirlAngle(float swirlAngle) {
		this.swirlAngle = swirlAngle;
		this.lastSwirlAngle = swirlAngle;
	}

	public float getSwirlAngle() {
		return this.swirlAngle;
	}

	@Override
	public void postShader(CShaderGroup shaderGroup, float partialTicks) {
		if(this.swirlEffect == null) {
			this.swirlEffect = (SwirlEffect) new SwirlEffect().init();
		}

		this.swirlAngle = this.swirlAngle + (this.swirlAngle - this.lastSwirlAngle) * partialTicks;
		this.lastSwirlAngle = this.swirlAngle;

		if(this.swirlAngle != 0.0F) {
			//Render swirl to blit buffer
			this.swirlEffect.setAngle(this.swirlAngle);
			this.swirlEffect.apply(Minecraft.getMinecraft().getFramebuffer().framebufferTexture, this.getBlitBuffer(), null, Minecraft.getMinecraft().getFramebuffer(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

			//Render blit buffer to main buffer
			GL11.glPushMatrix();

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0.0D, scaledResolution.getScaledWidth_double(), scaledResolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glTranslatef(0.0F, 0.0F, -2000.0F);

			double renderWidth = scaledResolution.getScaledWidth();
			double renderHeight = scaledResolution.getScaledHeight();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getBlitBuffer().framebufferTexture);
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glTexCoord2d(0.0D, 1.0D);
			GL11.glVertex2d(0, 0);
			GL11.glTexCoord2d(0.0D, 0.0D);
			GL11.glVertex2d(0, renderHeight);
			GL11.glTexCoord2d(1.0D, 0.0D);
			GL11.glVertex2d(renderWidth, renderHeight);
			GL11.glTexCoord2d(1.0D, 0.0D);
			GL11.glVertex2d(renderWidth, renderHeight);
			GL11.glTexCoord2d(1.0D, 1.0D);
			GL11.glVertex2d(renderWidth, 0);
			GL11.glTexCoord2d(0.0D, 1.0D);
			GL11.glVertex2d(0, 0);
			GL11.glEnd();

			GL11.glPopMatrix();
		}
	}
}
