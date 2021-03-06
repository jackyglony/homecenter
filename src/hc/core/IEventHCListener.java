package hc.core;


public abstract class IEventHCListener {
	protected boolean enableSameEventTag = false;
	
	public IEventHCListener() {
	}
	
	/**
	 * 允许使用相同的EventTag，且不进行覆盖
	 * @param enableSameEventTag
	 */
	public IEventHCListener(boolean enableSameEventTag) {
		this.enableSameEventTag = enableSameEventTag;
	}
	
	public abstract byte getEventTag();

	/**
	 * 返回true，表示停止后继其它的侦听的响应操作。
	 * @param obj
	 * @return
	 */
	public abstract boolean action(final byte[] bs);

}
