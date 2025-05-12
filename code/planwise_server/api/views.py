# api/views.py

from rest_framework import viewsets, status
from rest_framework.decorators import api_view
from rest_framework.response import Response
from .models import Schedule
from .serializers import ScheduleSerializer
import requests
import json
from datetime import datetime

class ScheduleViewSet(viewsets.ModelViewSet):
    queryset = Schedule.objects.all()
    serializer_class = ScheduleSerializer

@api_view(['POST'])
def sync_all_to_cloud(request):
    """接收客户端的所有Schedule数据，替换服务器上的所有数据"""
    if request.method == 'POST':
        # 清除所有现有数据
        Schedule.objects.all().delete()
        
        # 如果是空列表，直接返回成功
        if not request.data:
            return Response({"message": "所有数据已清除"}, status=status.HTTP_200_OK)
        
        # 批量创建新数据
        serializer = ScheduleSerializer(data=request.data, many=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

@api_view(['GET'])
def get_all_from_cloud(request):
    """返回服务器上的所有Schedule数据"""
    if request.method == 'GET':
        schedules = Schedule.objects.all()
        serializer = ScheduleSerializer(schedules, many=True)
        return Response(serializer.data)

@api_view(['POST'])
def get_ai_suggestion(request):
    """获取AI对日程的建议"""
    if request.method == 'POST':
        try:
            # 获取请求数据
            schedule_data = request.data
            
            # 构建提示词
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M")
            
            # 获取日程信息
            title = schedule_data.get('title', '')
            description = schedule_data.get('description', '')
            scheduled_date = schedule_data.get('scheduled_date', '')
            location = schedule_data.get('location', '')
            category = schedule_data.get('category', '')
            is_completed = schedule_data.get('is_completed', False)
            
            # 构建提示文本
            prompt = f"""现在是{current_time}，我计划在{location}于{scheduled_date}前完成{title}，"""
            if description:
                prompt += f"其细节是{description}，"
            prompt += f"这个任务属于{category}类别，"
            prompt += "目前" + ("已经完成" if is_completed else "尚未完成") + "。"
            prompt += """
            请给我一些关于这个任务的建议，注意要求：
            1. 必须使用纯文本格式，不要使用任何Markdown语法（如*加粗*、_斜体_、#标题等）
            2. 使用平实的聊天语气
            3. 直接给出建议，不要添加"以下是我的建议"之类的开场白
            4. 不要在回复结尾添加"需要更多帮助吗"、"还有其他问题吗"之类的结束语
            5. 可以内容丰富一点，但要整体有条例，是一个很好的建议视角
            """
            
            # 调用 DeepSeek API
            deepseek_api_key = "sk-76355a715ce345bc93e5b33431eb92fd"
            headers = {
                "Content-Type": "application/json",
                "Authorization": f"Bearer {deepseek_api_key}"
            }
            
            data = {
                "model": "deepseek-chat",
                "messages": [
                    {"role": "user", "content": prompt}
                ],
                "temperature": 0.7,
                "max_tokens": 300
            }
            
            response = requests.post(
                "https://api.deepseek.com/v1/chat/completions",
                headers=headers,
                data=json.dumps(data)
            )
            
            # 检查响应
            if response.status_code == 200:
                result = response.json()
                ai_suggestion = result['choices'][0]['message']['content']
                
                # 额外处理，移除可能的Markdown语法
                ai_suggestion = ai_suggestion.replace('*', '')
                ai_suggestion = ai_suggestion.replace('_', '')
                ai_suggestion = ai_suggestion.replace('#', '')
                ai_suggestion = ai_suggestion.replace('```', '')
                
                print(prompt)
                print(ai_suggestion)
                return Response({"suggestion": ai_suggestion}, status=status.HTTP_200_OK)
                
            else:
                return Response(
                    {"error": f"DeepSeek API error: {response.text}"},
                    status=status.HTTP_500_INTERNAL_SERVER_ERROR
                )
                
        except Exception as e:
            return Response(
                {"error": f"Error generating AI suggestion: {str(e)}"},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )