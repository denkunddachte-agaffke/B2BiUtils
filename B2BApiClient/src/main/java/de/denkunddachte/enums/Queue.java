/*
  Copyright 2018 - 2023 denk & dachte Software GmbH

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package de.denkunddachte.enums;

public enum Queue {
	Q1(1), Q2(2), Q3(3), Q4(4), Q5(5), Q6(6), Q7(7), Q8(8), Q9(9);

	private final int queue;

	private Queue(int queue) {
		this.queue = queue;
	}

	public int getQueueNumber() {
		return queue;
	}

	public String getDisplay() {
		return Integer.toString(queue);
	}

	public static Queue getQueue(int queue) {
		for (Queue v : Queue.values()) {
			if (queue == v.getQueueNumber())
				return v;
		}
		throw new IllegalArgumentException("No Queue " + queue + "!");
	}
}
