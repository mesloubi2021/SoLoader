/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.soloader.recovery;

import com.facebook.soloader.LogUtil;
import com.facebook.soloader.SoLoader;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class BaseApkPathHistory {
  @GuardedBy("this")
  private final String[] mRecentPaths;

  @GuardedBy("this")
  private int mCounter;

  public BaseApkPathHistory(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException();
    }

    mRecentPaths = new String[length];
    mCounter = 0;
  }

  public synchronized boolean recordPathIfNew(String path) {
    for (String oldPath : mRecentPaths) {
      if (path.equals(oldPath)) {
        return false;
      }
    }

    StringBuilder sb = new StringBuilder("Recording new base apk path: ").append(path).append("\n");
    report(sb);
    LogUtil.w(SoLoader.TAG, sb.toString());

    mRecentPaths[mCounter++ % mRecentPaths.length] = path;
    return true;
  }

  public synchronized void report(StringBuilder sb) {
    sb.append("Previously recorded ").append(mCounter).append(" base apk paths.");
    if (mCounter > 0) {
      sb.append(" Most recent ones:");
    }
    for (int i = 0; i < mRecentPaths.length; ++i) {
      int index = mCounter - i - 1;
      if (index >= 0) {
        sb.append("\n").append(mRecentPaths[index % mRecentPaths.length]);
      }
    }
  }

  public String report() {
    StringBuilder sb = new StringBuilder();
    report(sb);
    return sb.toString();
  }

  public synchronized int size() {
    return mCounter;
  }
}
