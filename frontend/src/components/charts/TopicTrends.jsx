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

const generateMonthRange = (startDateStr, endDateStr) => {
  const start = new Date(startDateStr);
  const end = new Date(endDateStr);
  const dates = [];

  let current = new Date(start.getFullYear(), start.getMonth(), 1);
  const endTime = end.getTime();

  while (current.getTime() <= endTime) {
    const year = current.getFullYear();
    const month = String(current.getMonth() + 1).padStart(2, '0');
    dates.push(`${year}-${month}`);
    
    current.setMonth(current.getMonth() + 1);
  }
  return dates;
};

const TopicTrends = () => {
  const [selectedGroup, setSelectedGroup] = useState("Java Core");
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [availableTopics, setAvailableTopics] = useState([]);

  const QUERY_CONFIG = {
    startDate: '2024-12-01',
    endDate: '2025-11-30',
    granularity: 'monthly'
  };

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
          ...QUERY_CONFIG
        }
      });

      const rawData = response.data.data;
      const topicList = response.data.topics || [];
      const fullDateRange = generateMonthRange(QUERY_CONFIG.startDate, QUERY_CONFIG.endDate);


      const chartData = fullDateRange.map(date => {
        const point = { date };
        
        topicList.forEach(topic => {
          const topicPoints = rawData[topic] || [];
          const match = topicPoints.find(p => p.date === date);
          point[topic] = match ? match.value : 0;
        });

        return point;
      });

      setData(chartData);
      setAvailableTopics(topicList);

    } catch (error) {
      console.error("Error fetching trends", error);
      setData([]); 
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
            {data.length > 0 ? (
              <LineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis 
                  dataKey="date" 
                  className="text-xs" 
                  stroke="hsl(var(--muted-foreground))" 
                />
                <YAxis 
                  className="text-xs" 
                  stroke="hsl(var(--muted-foreground))" 
                />
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: 'hsl(var(--card))', 
                    borderColor: 'hsl(var(--border))',
                    color: 'hsl(var(--foreground))'
                  }}
                />
                <Legend wrapperStyle={{ paddingTop: '20px' }}/>
                
                {availableTopics.map((topic, index) => (
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
            ) : (
              <div className="flex h-full items-center justify-center text-muted-foreground">
                No data available for this range.
              </div>
            )}
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
};

export default TopicTrends;
