import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import ChartSkeleton from '../ChartSkeleton';

const QuestionSolvability = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    axios.get('/api/stats/question-solvability')
      .then(res => setData(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading || !data) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {[1, 2, 3].map((i) => (
          <ChartSkeleton key={i} height="h-[300px]" />
        ))}
      </div>
    );
  }

  return (
    <div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {data.factors.map((factor, idx) => (
          <div key={idx} className="border rounded-lg p-4 bg-muted/10">
            <h4 className="text-sm font-medium mb-4 text-center h-10 flex items-center justify-center">
              {factor.factorName} ({factor.unit})
            </h4>
            <div className="h-[200px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={[
                    { name: 'Solvable', value: factor.solvableValue },
                    { name: 'Hard', value: factor.hardToSolveValue }
                  ]}
                  margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                >
                  <CartesianGrid strokeDasharray="3 3" vertical={false} className="stroke-muted"/>
                  <XAxis dataKey="name" className="text-xs" />
                  <YAxis className="text-xs"/>
                  <Tooltip 
                    cursor={{fill: 'transparent'}}
                    contentStyle={{ backgroundColor: 'hsl(var(--card))', borderColor: 'hsl(var(--border))', color: 'hsl(var(--foreground))' }}
                  />
                  <Bar dataKey="value" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} barSize={40} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        ))}
      </div>
      <div className="mt-6 flex justify-center gap-8 text-sm text-muted-foreground">
        <div className="flex flex-col items-center">
          <span className="font-bold text-2xl text-foreground">{data.groupCounts.Solvable}</span>
          <span>Total Solvable</span>
        </div>
        <div className="flex flex-col items-center">
          <span className="font-bold text-2xl text-foreground">{data.groupCounts['Hard-to-Solve']}</span>
          <span>Total Hard-to-Solve</span>
        </div>
      </div>
    </div>
  );
};

export default QuestionSolvability;
