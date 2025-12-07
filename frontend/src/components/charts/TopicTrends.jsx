import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const TOPIC_GROUPS = {
  "Java Core": "stream,collections,multithreading,generics,reflection",
  "Java Runtime & Internals": "jvm,garbage-collection,javac",
  "Spring Ecosystem": "spring,spring-boot,spring-security,spring-data-jpa,spring-mvc",
  "Build Tools": "maven,gradle",
  "Servle Containers": "tomcat,jetty",
  "IDEs": "intellij-idea,eclipse",
  "Serialization": "json,jackson,gson,protobuf,xml"
};

const COLORS = ['#8884d8', '#82ca9d', '#ffc658', '#ff7300', '#0088fe'];

const TopicTrends = () => {
  const [selectedGroup, setSelectedGroup] = useState("Java Core");
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, [selectedGroup]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const topics = TOPIC_GROUPS[selectedGroup];
      // Fixed dates as per requirement
      const response = await axios.get('/api/stats/topic-trends', {
        params: {
          topics: topics,
          startDate: '2024-12-01',
          endDate: '2025-11-30',
          granularity: 'monthly'
        }
      });

      const rawData = response.data.data;
      
      // Transform data for Recharts: [{ date: '2024-12', stream: 10, collections: 5 }, ...]
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
            className={`px-4 py-2 text-sm rounded-md transition-colors ${
              selectedGroup === group 
                ? 'bg-primary text-primary-foreground' 
                : 'bg-secondary text-secondary-foreground hover:bg-secondary/80'
            }`}
          >
            {group}
          </button>
        ))}
      </div>

      <div className="h-[400px] w-full">
        {loading ? (
          <div className="h-full flex items-center justify-center text-muted-foreground">Loading...</div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
              <XAxis dataKey="date" className="text-xs" />
              <YAxis className="text-xs" />
              <Tooltip 
                contentStyle={{ backgroundColor: 'hsl(var(--card))', borderColor: 'hsl(var(--border))', color: 'hsl(var(--foreground))' }}
              />
              <Legend />
              {TOPIC_GROUPS[selectedGroup].split(',').map((topic, index) => (
                <Line 
                  key={topic} 
                  type="monotone" 
                  dataKey={topic} 
                  stroke={COLORS[index % COLORS.length]} 
                  activeDot={{ r: 8 }}
                  strokeWidth={2}
                  animationDuration={1500}
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
