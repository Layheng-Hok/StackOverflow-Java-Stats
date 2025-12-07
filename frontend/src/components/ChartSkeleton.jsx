import React from 'react';
import { Loader2 } from 'lucide-react';

const ChartSkeleton = ({ height = "h-[350px]", className = "" }) => {
  return (
    <div className={`relative w-full ${height} rounded-lg border bg-card/50 p-6 shadow-sm overflow-hidden ${className}`}>
      {/* Centered Loading Spinner with Text */}
      <div className="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 text-muted-foreground">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <span className="text-sm font-medium animate-pulse">Gathering data...</span>
      </div>

      {/* Background Pulse Animation (Simulating a chart) */}
      <div className="flex h-full w-full items-end gap-4 opacity-20 blur-[1px]">
        <div className="h-[40%] w-full animate-pulse rounded-t bg-muted delay-75"></div>
        <div className="h-[70%] w-full animate-pulse rounded-t bg-muted delay-100"></div>
        <div className="h-[50%] w-full animate-pulse rounded-t bg-muted delay-150"></div>
        <div className="h-[85%] w-full animate-pulse rounded-t bg-muted delay-200"></div>
        <div className="h-[60%] w-full animate-pulse rounded-t bg-muted delay-300"></div>
        <div className="h-[45%] w-full animate-pulse rounded-t bg-muted delay-75"></div>
      </div>
    </div>
  );
};

export default ChartSkeleton;
