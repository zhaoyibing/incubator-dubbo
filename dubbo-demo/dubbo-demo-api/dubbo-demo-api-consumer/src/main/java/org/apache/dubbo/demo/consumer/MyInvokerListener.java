package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.listener.InvokerListenerAdapter;

public class MyInvokerListener extends InvokerListenerAdapter {

	
	@Override
	public void referred(Invoker<?> invoker) throws RpcException {
		super.referred(invoker);
		System.out.println("---- in  " + MyInvokerListener.class.getName());
	}
}
