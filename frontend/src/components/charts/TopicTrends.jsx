import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import ChartSkeleton from '../ChartSkeleton';

const TOPIC_GROUPS = {
  "Java Core": "stream,collections,multithreading,generics,reflection",
  "Java Runtime & Internals": "jvm,garbage-collection,javac",
  "Spring Ecosystem": "spring,spring-boot,spring-security,spring-data-jpa,spring-mvc",
  "Servlet Containers": "tomcat,jetty",
  "Build Tools": "maven,gradle",
  "IDEs": "intellij-idea,eclipse",
  "Serialization": "json,jackson,gson,protobuf,xml"
};

const COLORS = ['#8884d8', '#82ca9d', '#ffc658', '#ff7300', '#0088fe'];

const TopicTrends = () => {
  const [selectedGroup, setSelectedGroup] = useState("Java Core");
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, [selectedGroup]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const topics = TOPIC_GROUPS[selectedGroup];
      const response = await axios.get('/api/stats/topic-trends', {
        params: {
          topics: topics,
          startDate: '2024-12-01',
          endDate: '2025-11-30',
          granularity: 'monthly'
        }
      });

      const rawData = response.data.data;
      
      const chartData = [];
      const dates = Object.values(rawData)[0]?.map(t => t.date) || [];
      
      dates.forEach((date, index) => {
        const point = { date };
        response.data.topics.forEach(topic => {
          point[topic] = rawData[topic][index]?.value || 0;
        });
        chartData.push(point);
      });

      setData(chartData);
    } catch (error) {
      console.error("Error fetching trends", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-2 mb-4">
        {Object.keys(TOPIC_GROUPS).map(group => (
          <button
            key={group}
            onClick={() => setSelectedGroup(group)}
            className={`px-4 py-2 text-sm rounded-md transition-all ${
              selectedGroup === group 
                ? 'bg-primary text-primary-foreground shadow-md' 
                : 'bg-secondary text-secondary-foreground hover:bg-secondary/80'
            }`}
          >
            {group}
          </button>
        ))}
      </div>

      <div className="h-[400px] w-full">
        {loading ? (
          <ChartSkeleton height="h-full" />
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
              <XAxis dataKey="date" className="text-xs" stroke="hsl(var(--muted-foreground))" />
              <YAxis className="text-xs" stroke="hsl(var(--muted-foreground))" />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: 'hsl(var(--card))', 
                  borderColor: 'hsl(var(--border))',
                  color: 'hsl(var(--foreground))'
                }}
              />
              <Legend wrapperStyle={{ paddingTop: '20px' }}/>
              {Object.keys(data[0] || {}).filter(k => k !== 'date').map((topic, index) => (
                <Line
                  key={topic}
                  type="monotone"
                  dataKey={topic}
                  stroke={COLORS[index % COLORS.length]}
                  strokeWidth={2}
                  dot={{ r: 4 }}
                  activeDot={{ r: 6 }}
                />
              ))}
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
};

export default TopicTrends;
